package com.codenjoy.dojo.snakebattle.client

import com.codenjoy.dojo.client.Solver
import com.codenjoy.dojo.client.WebSocketRunner
import com.codenjoy.dojo.services.Dice
import com.codenjoy.dojo.services.Direction
import com.codenjoy.dojo.services.Point
import com.codenjoy.dojo.services.PointImpl
import com.codenjoy.dojo.services.RandomDice
import com.codenjoy.dojo.snakebattle.model.Elements

class GroovySolver implements Solver<Board> {

    private Dice dice
    private Board board
    private int furyCount = 0
    private Direction lastDirection = null

    GroovySolver(Dice dice) {
        this.dice = dice;
    }

    def blackList = [[20,21], [21,21], [22,21]]

    Direction getCurrentDirection() {

        if (board.isAt(board.me.x, board.me.y, Elements.HEAD_RIGHT)) return Direction.RIGHT
        if (board.isAt(board.me.x, board.me.y, Elements.HEAD_LEFT)) return Direction.LEFT
        if (board.isAt(board.me.x, board.me.y, Elements.HEAD_UP)) return Direction.UP
        if (board.isAt(board.me.x, board.me.y, Elements.HEAD_DOWN)) return Direction.DOWN
    }

    Direction getByPoint(Point point) {

        Direction res

        if (!point) {
            return Direction.RIGHT
        }

        if (point.x > board.me.x) {
            if (point.y > board.me.y) {
                res = (Math.abs(point.y - board.me.y) > Math.abs(point.x - board.me.x)) ? Direction.UP : Direction.RIGHT
            } else {
                res = (Math.abs(point.y - board.me.y) > Math.abs(point.x - board.me.x)) ? Direction.DOWN : Direction.RIGHT
            }
        } else {
            if (point.y > board.me.y) {
                res = (Math.abs(point.y - board.me.y) > Math.abs(point.x - board.me.x)) ? Direction.UP : Direction.LEFT
            } else {
                res = (Math.abs(point.y - board.me.y) > Math.abs(point.x - board.me.x)) ? Direction.DOWN : Direction.LEFT
            }
        }

        res
    }

    int length() {
        board.get(Elements.BODY_VERTICAL, Elements.BODY_HORIZONTAL, Elements.BODY_LEFT_DOWN, Elements.BODY_LEFT_UP, Elements.BODY_RIGHT_DOWN, Elements.BODY_RIGHT_UP).size()
    }

    List<Direction> allowDirection() {
        def res = [Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT]
        def removed = []

        def curr = lastDirection ?: getCurrentDirection()

        switch (curr) {
            case Direction.RIGHT: res.remove(Direction.LEFT); break
            case Direction.UP: res.remove(Direction.DOWN); break
            case Direction.LEFT: res.remove(Direction.RIGHT); break
            case Direction.DOWN: res.remove(Direction.UP); break
        }

        List<Elements> list = [Elements.WALL, Elements.START_FLOOR, Elements.TAIL_END_RIGHT, Elements.TAIL_END_DOWN, Elements.TAIL_END_UP]

        if (length() < 5) {
            list.add(Elements.STONE)
        }

        Elements[] elements = list.toArray() as Elements[]

        for (def e : res) {

            switch (e) {
                case Direction.LEFT:
                    if (board.isAt(board.me.x - 1, board.me.y, elements)) {
                        removed.add(e)
                    }
                    break
                case Direction.RIGHT:
                    if (board.isAt(board.me.x + 1, board.me.y, elements)) {
                        removed.add(e)
                    }
                    break
                case Direction.UP:
                    if (board.isAt(board.me.x, board.me.y + 1, elements)) {
                        removed.add(e)
                    }
                    break
                case Direction.DOWN:
                    if (board.isAt(board.me.x, board.me.y - 1, elements)) {
                        removed.add(e)
                    }
                    break
            }
        }

        res - removed
    }

    Direction getApple() {

        def stone = board.get(Elements.STONE).sort { it.distance(board.me) }.find()
        def gold = board.get(Elements.GOLD).sort { it.distance(board.me) }.find()
        def apple = board.get(Elements.APPLE, Elements.FURY_PILL).sort { it.distance(board.me) }.find()

        if (!gold) {
            return getByPoint(apple)
        }

        if (!apple) {
            return Direction.RIGHT
        }

        if (length() > 5) {
            if (board.me.distance(stone) < (board.me.distance(gold) / 2) || board.me.distance(stone) < (board.me.distance(apple) / 2)) {
                getByPoint(stone)
            }
        }

        if (board.me.distance(apple) < (board.me.distance(gold) / 4)) {
            getByPoint(apple)
        } else {
            getByPoint(gold)
        }
    }

    @Override
    String get(Board board) {

        this.board = board
        def res = null
        if (board.isGameOver()) return ""

        def direction = getApple()

        def allowed = allowDirection()

        println "My position ${board.me}"
        println "My length ${length()}"

        if (allowed.contains(direction)) {
            res = direction
        } else {
            res = allowed.find()
        }

        lastDirection = res
        res
        //def element = getElementByDirection(direction)

        //if (element == Elements.FURY_PILL) {
        //    furyCount = 10
        //}

        //println "Current ${getCurrentDirection()}"

        //correct(direction) ?: correct(Direction.RIGHT)
    }

    Elements getElementByDirection(Direction direction) {

        switch (direction) {
            case Direction.RIGHT: return board.getAllAt(board.me.x + 1, board.me.y).find()
            case Direction.LEFT: return board.getAllAt(board.me.x - 1, board.me.y).find()
            case Direction.UP: return board.getAllAt(board.me.x + 1, board.me.y + 1).find()
            case Direction.DOWN: return board.getAllAt(board.me.x, board.me.y - 1).find()
        }
    }

    static void main(String[] args) {

                def runner = WebSocketRunner.runClient(
                        // paste here board page url from browser after registration
                        "https://game2.epam-bot-challenge.com.ua/codenjoy-contest/board/player/yvshvets@gmail.com?code=994604177213209479",
                        new GroovySolver(new RandomDice()),
                        new Board())
    }
}
