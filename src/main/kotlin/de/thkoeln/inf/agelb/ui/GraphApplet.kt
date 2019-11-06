package de.thkoeln.inf.agelb.ui

import processing.core.PApplet
import processing.core.PVector
import kotlin.math.pow
import controlP5.*
import controlP5.Textfield
import controlP5.ControlEvent

class GraphApplet(val config: Config) : PApplet()
{
    data class Config(
        val width: Int,
        val height: Int,
        val isResizable: Boolean,
        val backgroundColor: Int,
        val scrollDragTolerance: Float,
        val scrollingMouseButton: Int,
        val node: Node
    ) {
        data class Node(
            val radius: Float,
            val padding: Float,
            val paddingColor: Int,
            val fillColor: Int,
            val selectedFillColor: Int,
            val highlightedFillColor: Int,
            val strokeColor: Int,
            val strokeWeight: Float
        )
    }

    companion object Factory
    {
        fun run(config: Config) = GraphApplet(config).runSketch()
    }

    fun distSq(x1: Float, y1: Float, x2: Float, y2: Float)
            = (x1 - x2).pow(2) + (y1 - y2).pow(2)

    inner class Node(var x: Float, var y: Float,
                     radius: Float = config.node.radius,
                     padding: Float = config.node.padding)
    {
        var radius: Float = radius
            set(value) { if (value > 0) field = value }

        var padding: Float = padding
            set (value) { if (value > 0) field = value }

        val realPadding
            get() = this.padding + config.node.strokeWeight

        val diameter: Float
            get() = radius * 2

        fun draw()
        {
            ellipse(x, y, diameter, diameter)
        }

        fun drawPadding()
        {
            val d = radius + padding
            ellipse(x, y, d * 2, d * 2)
        }

        fun distanceTo(other: Node) = sqrt(squaredDistanceTo(other))

        fun squaredDistanceTo(x: Float, y: Float)
                = distSq(this.x, this.y, x, y)

        fun squaredDistanceTo(other: Node)
                = squaredDistanceTo(other.x, other.y)

        fun overlapsWith(other: Node, includePadding: Boolean = false)
                : Boolean
        {
            val dist = squaredDistanceTo(other)
            val r1 = this.radius
            val r2 = other.radius

            var p = 0f
            if (includePadding)
                p = max(this.realPadding, other.realPadding)

            return dist <= (r1 + r2 + p).pow(2)
        }

        fun contains(other: Node) = contains(other.x, other.y)
        fun contains(x: Float, y: Float)
                = squaredDistanceTo(x, y) < radius.pow(2)
    }

    // Implemented as data class so that its
    // hash code solely depends on its properties.
    data class Edge(val between: Pair<Node, Node>)
    {
        val first: Node get() = between.first
        val second: Node get() = between.second

        fun opposite() = Edge(Pair(second, first))
    }

    private val nodeList = mutableListOf<Node>()
    private val edgeSet = mutableSetOf<Edge>()

    fun controlEvent(event: ControlEvent)
    {
        println("controlEvent: accessing a string from controller '"
            + event.name + "': " + event.stringValue )
    }

    override fun setup()
    {
        surface.setResizable(config.isResizable)
        surface.setSize(config.width, config.height)

        val topLeftX = displayWidth / 2 - config.width / 2
        val topLeftY = displayHeight / 2 - config.height / 2
        surface.setLocation(topLeftX, topLeftY)

        previousWidth = width
        previousHeight = height

        textField = cp5.addTextfield("textValue")
            .setPosition(20f, 170f)
            .setFont(createFont("Consolas", 14f))
            .setSize(50, 20)
            .setAutoClear(false)
            .setCaptionLabel("")
            .setFocus(true)
            .setColorBackground(color(255, 140))
            .setColorForeground(color(255, 140))
            .setColorValue(color(0))
            .setColorActive(color(200))
            .setColorCursor(color(0))

        // cp5.isAutoDraw = false
    }

    override fun draw()
    {
        if (width != previousWidth || height != previousHeight) {
            val dx = (width - previousWidth) / 2
            val dy = (height - previousHeight) / 2
            scrollBy(dx, dy)
            previousWidth = width
            previousHeight = height
        }

        clear()
        background(config.backgroundColor)

        noStroke()
        fill(config.node.paddingColor)
        nodeList.forEach { it.drawPadding() }

        strokeWeight(config.node.strokeWeight)

        stroke(config.node.strokeColor)
        fill(config.node.fillColor)
        nodeList.forEach { it.draw() }

        highlightedNode?.let {
            stroke(config.node.strokeColor)
            fill(config.node.highlightedFillColor)
            it.draw()
        }

        selectedNode?.let {
            stroke(config.node.strokeColor)
            fill(config.node.selectedFillColor)
            it.draw()
        }

        // cp5.draw() // Draw the text elements before the edges.

        // TODO: Move detection of highlighted edge to the mouseMove() listener.
        var haveEdge = false

        edgeSet.forEach { edge ->
            val a = PVector(edge.first.x, edge.first.y)
            val b = PVector(edge.second.x, edge.second.y)
            val aToB = b.sub(a).setMag(edge.first.radius)

            val x1 = edge.first.x + aToB.x
            val y1 = edge.first.y + aToB.y
            val x2 = edge.second.x - aToB.x
            val y2 = edge.second.y - aToB.y

            stroke(config.node.strokeColor)

            if (!haveEdge && selectedNode == null && highlightedNode == null) {
                val bx = min(x1, x2) - 10.0f
                val by = min(y1, y2) - 10.0f
                val dx = abs(x1 - x2) + 20.0f
                val dy = abs(y1 - y2) + 20.0f
                val insideBox = boxContains(bx, by, dx, dy, mouseX.toFloat(), mouseY.toFloat())
                if (insideBox) {
                    val dist = lineDistance(x1, y1, x2, y2, mouseX.toFloat(), mouseY.toFloat())
                    if (dist < 10.0f) {
                        haveEdge = true
                        highlightedEdge = edge
                        stroke(color(20, 120, 220))
                    }
                }
            }

            line(x1, y1, x2, y2)
        }

        if (!haveEdge)
            highlightedEdge = null

        selectedNode?.let { node ->
            val nodeVector = PVector(node.x, node.y)
            val mouseVector = PVector(mouseX.toFloat(), mouseY.toFloat())
            val direction = mouseVector.sub(nodeVector)
            if (direction.mag() < node.radius)
                return

            direction.setMag(node.radius)

            stroke(0, 80f)
            line(node.x + direction.x, node.y + direction.y, mouseX.toFloat(), mouseY.toFloat())
        }
    }

    // TODO: For testing only.
    private val cp5: ControlP5 by lazy { ControlP5(this) }
    private var textField: Textfield? = null

    private var createdNode: Node? = null
    private var clickedNode: Node? = null
    private var selectedNode: Node? = null
    private var highlightedNode: Node? = null
    private var highlightedEdge: Edge? = null

    private var clickedX: Float = 0f
    private var clickedY: Float = 0f
    private var clickedOffsetX: Float = 0f
    private var clickedOffsetY: Float = 0f

    private var isScrolling: Boolean = false
    private var isDragging: Boolean = false

    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

    override fun mousePressed()
    {
        // TODO: holding SHIFT while connecting nodes should
        //  automatically select the node that was clicked on last.

        // TODO: while connecting nodes, left-clicking on an empty spot
        //  should not create a new node.

        // TODO: right-clicking on a node should delete it.

        // TODO: right-clicking anywhere while connecting nodes should
        //  cancel the connection of nodes.

        // TODO: show how many nodes are off-screen at the side of the window.
        //  additionally: maybe show a map of all existing nodes.

        // Click on a node:
        // > single click: selects and highlights node.
        //   a line appears starting from that node, representing an edge.
        //   clicking on a different node connects both nodes through said edge.
        // > click and drag: moves the node to the cursor position.
        //   the dragged node will move around a node

        // Click anywhere else:
        // > click and drag after a certain tolerance distance:
        //   scrolls i.e. moves all elements in the window.

        // Click on a spot where a node can be placed:
        // > single click: creates a node at that position.

        val x = mouseX.toFloat()
        val y = mouseY.toFloat()
        val node = Node(x, y)

        clickedX = x
        clickedY = y

        var isNodePlaceable = true
        for (other in nodeList) {
            if (other.contains(x, y)) {
                isNodePlaceable = false
                clickedNode = other
                break
            }

            if (other.overlapsWith(node, includePadding = true))
                isNodePlaceable = false
        }

        if (isNodePlaceable && mouseButton == LEFT)
            createdNode = node
    }

    override fun mouseDragged()
    {
        clickedNode?.also { node ->
            // TODO: scrolling mouse button -> dragging mouse button
            if (mouseButton != config.scrollingMouseButton)
                return@also

            if (!isDragging) {
                val x = mouseX.toFloat()
                val y = mouseY.toFloat()
                val tolerance = config.scrollDragTolerance.pow(2)
                isDragging = distSq(x, y, clickedX, clickedY) > tolerance
                clickedOffsetX = mouseX - node.x
                clickedOffsetY = mouseY - node.y
            }

            if (isDragging) {
                node.x = mouseX.toFloat() - clickedOffsetX
                node.y = mouseY.toFloat() - clickedOffsetY

                nodeList.firstOrNull {
                    node != it && node.overlapsWith(it, includePadding = true)
                } ?.let { colliding ->
                    val collidingVector = PVector(colliding.x, colliding.y)
                    val mouseVector = PVector(node.x, node.y)

                    val mag = node.radius + colliding.radius + colliding.realPadding
                    val direction = mouseVector.sub(collidingVector).setMag(mag)

                    node.x = colliding.x + direction.x
                    node.y = colliding.y + direction.y
                }
            }
        } ?: run {
            if (mouseButton != config.scrollingMouseButton)
                return@run

            if (!isScrolling) {
                val x = mouseX.toFloat()
                val y = mouseY.toFloat()
                val tolerance = config.scrollDragTolerance.pow(2)
                isScrolling = distSq(x, y, clickedX, clickedY) > tolerance
            }

            if (isScrolling) {
                // Do not place the created node when the
                // user decides to start scrolling.
                createdNode = null
                // Deselect the node when the user starts dragging.
                selectedNode = null
                scrollBy(mouseX - pmouseX, mouseY - pmouseY)
            }
        }
    }

    override fun mouseReleased()
    {
        clickedNode?.takeIf {
            // We're not selecting a node if it was dragged around.
            // Also, only select nodes with the right mouse button.
            !isDragging && mouseButton == LEFT
        } ?.let { clicked ->
            selectedNode?.takeIf {
                // Only add an edge if the two nodes are different.
                it != clicked
            } ?.also { selected ->
                val edge = Edge(selected to clicked)

                if (edge in edgeSet)
                    edgeSet.remove(edge)
                else if (edge.opposite() in edgeSet)
                    edgeSet.remove(edge.opposite())
                else
                    edgeSet.add(edge)

                selectedNode = null
            } ?: run {
                // Either we selected a different node,
                // or the node was deselected.
                selectedNode = clicked.takeIf { it != selectedNode }
            }
        }

        highlightedEdge?.takeIf {
            mouseButton == RIGHT
        } ?.let { edge ->
            println("edge right-clicked")

            val font = createFont("arial", 20f)
            cp5.addTextfield("input")
                .setPosition(mouseX.toFloat(), mouseY.toFloat())
                .setSize(200,40)
                .setFont(font)
                .setFocus(true)
                .setColor(color(255,0,0))

        }

        createdNode?.let {
            nodeList.add(it)
            highlightedNode = it
        }
        createdNode = null
        clickedNode = null

        isDragging = false
        isScrolling = false

        clickedOffsetX = 0f
        clickedOffsetY = 0f
    }

    override fun mouseMoved()
    {
        val x = mouseX.toFloat()
        val y = mouseY.toFloat()

        highlightedNode = nodeList.firstOrNull { it.contains(x, y) }
    }

    fun scrollBy(dx: Int, dy: Int)
    {
        nodeList.forEach { node ->
            node.x += dx
            node.y += dy
        }
    }

    /* == Helpers == */
    // TODO: Move somewhere else later.

    // source: https://gist.github.com/lennerd/10259253
    fun lineDistance(x1: Float, y1: Float,
                     x2: Float, y2: Float,
                     mX: Float, mY: Float) : Float
    {
        val lineStart = PVector(x1, y1)
        val lineEnd = PVector(x2, y2)
        val mouse = PVector(mX, mY)

        val projection: PVector
        val temp: PVector

        temp = PVector.sub(lineEnd, lineStart)

        val lineLength = temp.x * temp.x + temp.y * temp.y //lineStart.dist(lineEnd);

        if (lineLength == 0f) {
            return mouse.dist(lineStart)
        }

        val t = PVector.dot(PVector.sub(mouse, lineStart), temp) / lineLength

        if (t < 0f) {
            return mouse.dist(lineStart)
        }

        if (t > lineLength) {
            return mouse.dist(lineEnd)
        }

        projection = PVector.add(lineStart, PVector.mult(temp, t))

        return mouse.dist(projection)
    }

    fun boxContains(bx: Float, by: Float,
                    width: Float, height: Float,
                    mX: Float, mY: Float) : Boolean
            = mX > bx && mX < bx + width && mY > by && mY < by + height
}
