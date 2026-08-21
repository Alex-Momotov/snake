#!/usr/bin/env python3
"""Drive the terminal snake game under a pty so it can be observed and tested headlessly.

The game only emits CSI n A/B/C/D (cursor moves), CSI ?25l/h and CSI ..m (colour),
so a small VT100 subset is enough to reconstruct exactly what a human would see.

  script mode : send a timed sequence of keys, print text screenshots
  auto mode   : read the board and play it, steering greedily towards food

Both modes exit non-zero hints via the printed report, never by killing your terminal.
"""
import argparse, fcntl, os, pty, re, select, struct, subprocess, sys, termios, threading, time

CSI = re.compile(r'\x1b\[([0-9;?]*)([A-Za-z])')
KEYS = {'up': '\x1b[A', 'down': '\x1b[B', 'right': '\x1b[C', 'left': '\x1b[D'}
DELTAS = {'up': (-1, 0), 'down': (1, 0), 'left': (0, -1), 'right': (0, 1)}
OPPOSITE = {'up': 'down', 'down': 'up', 'left': 'right', 'right': 'left'}

EMPTY, WALL, BODY = '.', '+', 'o'
HEADS = '^v<>'
DOOR = 'D'
GOOD = '@p%!#$&8m?'      # treats worth eating, plus the friendly creatures
BAD = '*A W~'.replace(' ', '')   # rot and anything hostile
CELL_WIDTH = 3          # the game renders every cell as " c "


class Screen:
    """Minimal VT100: cursor moves clamp at the edges, exactly like a real terminal."""

    def __init__(self, rows, cols):
        self.rows, self.cols = rows, cols
        self.buf = [[' '] * cols for _ in range(rows)]
        self.r = self.c = 0
        self.pending = ''
        self.lock = threading.Lock()

    def _move(self, dr, dc):
        self.r = max(0, min(self.rows - 1, self.r + dr))
        self.c = max(0, min(self.cols - 1, self.c + dc))

    def _csi(self, params, final):
        n = int(params) if params.isdigit() else 1
        if   final == 'A': self._move(-n, 0)
        elif final == 'B': self._move(n, 0)
        elif final == 'C': self._move(0, n)
        elif final == 'D': self._move(0, -n)
        # 'm' (colour) and 'h'/'l' (cursor visibility) change no cell content

    def feed(self, data):
        with self.lock:
            data, self.pending = self.pending + data, ''
            i = 0
            while i < len(data):
                ch = data[i]
                if ch == '\x1b':
                    m = CSI.match(data, i)
                    if not m:
                        self.pending = data[i:]      # incomplete sequence, await more
                        return
                    self._csi(m.group(1), m.group(2))
                    i = m.end()
                elif ch == '\n':
                    self.r, self.c = min(self.rows - 1, self.r + 1), 0
                    i += 1
                elif ch == '\r':
                    self.c, i = 0, i + 1
                else:
                    self.buf[self.r][self.c] = ch
                    self.c += 1
                    if self.c >= self.cols:
                        self.c, self.r = 0, min(self.rows - 1, self.r + 1)
                    i += 1

    def lines(self):
        with self.lock:
            return [''.join(row) for row in self.buf]

    def dump(self):
        return '\n'.join(l.rstrip() for l in self.lines()).strip('\n')


def read_board(screen, rows, cols):
    """Extract the game grid from the screen as a list of strings, one char per cell."""
    lines = screen.lines()
    top = next((i for i, l in enumerate(lines)
                if len(l) > 1 and l[1] != ' '), None)
    if top is None or top + rows > len(lines):
        return None
    board = []
    for line in lines[top:top + rows]:
        board.append(''.join(line[c * CELL_WIDTH + 1] for c in range(cols)))
    return board


class Game:
    def __init__(self, jar, args, term_rows, term_cols):
        self.screen = Screen(term_rows, term_cols)
        self.master, slave = pty.openpty()
        fcntl.ioctl(self.master, termios.TIOCSWINSZ,
                    struct.pack('HHHH', term_rows, term_cols, 0, 0))
        cmd = ['java', '--enable-native-access=ALL-UNNAMED', '-jar', jar] + args
        self.proc = subprocess.Popen(cmd, stdin=slave, stdout=slave, stderr=slave,
                                     close_fds=True)
        os.close(slave)
        self.raw_log = ''          # every byte the game emitted, for escape-code assertions
        self.alive = True
        threading.Thread(target=self._pump, daemon=True).start()

    def _pump(self):
        while self.alive:
            try:
                if select.select([self.master], [], [], 0.05)[0]:
                    data = os.read(self.master, 65536)
                    if not data:
                        break
                    text = data.decode('utf-8', 'replace')
                    self.raw_log += text
                    self.screen.feed(text)
            except OSError:
                break

    def press(self, key):
        try:
            os.write(self.master, KEYS[key.lower()].encode())
        except OSError:
            pass                    # the game exited between our read and this write

    def dead(self):
        return self.proc.poll() is not None

    def stop(self):
        self.alive = False
        if not self.dead():
            self.proc.kill()
        self.proc.wait()
        os.close(self.master)


FACING = {'^': 'up', 'v': 'down', '<': 'left', '>': 'right'}


def find_head(board, previous=None):
    """The head draws itself as an arrow, so both its place and its heading are visible."""
    for r in range(len(board)):
        for c in range(len(board[0])):
            if board[r][c] in HEADS:
                return (r, c)
    return None


def find_facing(board):
    for row in board:
        for ch in row:
            if ch in FACING:
                return FACING[ch]
    return None


def choose(board, head, facing):
    """Head for treats and friendly creatures; step round anything that bites."""
    targets = [(r, c) for r in range(len(board)) for c in range(len(board[0]))
               if board[r][c] in GOOD or board[r][c] == DOOR]

    def safe(r, c):
        if not (0 <= r < len(board) and 0 <= c < len(board[0])):
            return False
        return board[r][c] in (EMPTY, DOOR) or board[r][c] in GOOD

    def scary(r, c):
        for dr in (-1, 0, 1):
            for dc in (-1, 0, 1):
                rr, cc = r + dr, c + dc
                if 0 <= rr < len(board) and 0 <= cc < len(board[0]) and board[rr][cc] in BAD:
                    return True
        return False

    options = []
    for key, (dr, dc) in DELTAS.items():
        if facing and key == OPPOSITE[facing]:
            continue
        r, c = head[0] + dr, head[1] + dc
        if not safe(r, c):
            continue
        cost = min((abs(r - tr) + abs(c - tc) for tr, tc in targets), default=0)
        options.append((scary(r, c), cost, key != facing, key))
    return min(options)[3] if options else facing


def run_script(g, events, run_ms):
    t0 = time.time()
    for ms, what in events:
        delay = t0 + ms / 1000 - time.time()
        if delay > 0:
            time.sleep(delay)
        if g.dead():
            break
        if what == 'snap':
            print(f'───── t={ms}ms ─────\n{g.screen.dump()}\n')
        else:
            g.press(what)
    remaining = t0 + run_ms / 1000 - time.time()
    if remaining > 0:
        time.sleep(remaining)


def run_auto(g, rows, cols, step_ms, run_ms, verbose):
    deadline = time.time() + run_ms / 1000
    previous, facing, turns = None, 'down', 0
    while time.time() < deadline and not g.dead():
        time.sleep(step_ms / 1000)
        board = read_board(g.screen, rows, cols)
        if board is None:
            continue
        head = find_head(board)
        previous = board
        if head is None:
            continue
        facing = find_facing(board) or facing      # trust the game, not our own guess
        want = choose(board, head, facing)
        if want != facing:
            g.press(want)
            facing, turns = want, turns + 1
            if verbose:
                print(f'turn {turns}: {want} from {head}')
    return previous


def main():
    p = argparse.ArgumentParser(description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('--jar', default='target/snake-1.0.jar')
    p.add_argument('--mode', choices=['script', 'auto'], default='auto')
    p.add_argument('--script', default='',
                   help='comma list of ms:key or ms:snap, e.g. "500:right,900:snap,1200:down"')
    p.add_argument('--run-ms', type=int, default=8000)
    p.add_argument('--rows', type=int, default=30)
    p.add_argument('--cols', type=int, default=23)
    p.add_argument('--step-ms', type=int, default=121)
    p.add_argument('-v', '--verbose', action='store_true')
    p.add_argument('game_args', nargs='*', help='extra flags passed straight to the game')
    a = p.parse_args()

    game_args = [f'--rows={a.rows}', f'--cols={a.cols}', f'--step_ms={a.step_ms}'] + a.game_args
    g = Game(a.jar, game_args, a.rows + 6, a.cols * CELL_WIDTH + 10)
    try:
        if a.mode == 'script':
            events = sorted((int(ms), what.strip())
                            for ms, _, what in (i.partition(':')
                                                for i in filter(None, a.script.split(','))))
            run_script(g, events, a.run_ms)
        else:
            run_auto(g, a.rows, a.cols, a.step_ms, a.run_ms, a.verbose)

        print(f'───── final t={a.run_ms}ms ─────\n{g.screen.dump()}')
        board = read_board(g.screen, a.rows, a.cols)
        length = sum(row.count(BODY) + sum(row.count(h) for h in HEADS)
                     for row in board) if board else -1
        food = sum(sum(row.count(g) for g in GOOD) for row in board) if board else -1
        over = board is not None and any('GAME OVER' in ''.join(l) for l in g.screen.lines())
        print(f'\n[snake_length={length} food_on_board={food} '
              f'game_over={over} exited={g.dead()} returncode={g.proc.poll()}]')
    finally:
        g.stop()


if __name__ == '__main__':
    main()
