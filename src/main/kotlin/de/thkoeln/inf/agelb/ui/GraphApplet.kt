package de.thkoeln.inf.agelb.ui

import de.thkoeln.inf.agelb.adt.graph.Graph
import processing.core.PApplet
import processing.core.PVector
import kotlin.math.pow
import controlP5.*
import controlP5.Textfield
import controlP5.ControlEvent
import de.thkoeln.inf.agelb.mst.*
import processing.core.PFont

private fun distSq(x1: Float, y1: Float, x2: Float, y2: Float)
        = (x1 - x2).pow(2) + (y1 - y2).pow(2)

class GraphApplet(val config: Config) : PApplet()
{
    data class Config(
        val width: Int,
        val height: Int,
        val isResizable: Boolean,
        val backgroundColor: Int,
        val scrollDragTolerance: Float,
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

    fun controlEvent(event: ControlEvent)
    {
        println(event.controller.name)

        edgeMap[event.id]?.let { edge ->
            edge.textField.isFocus = false
            graph.getEdge(edge.first.id to edge.second.id)?.let { graphEdge ->
                event.stringValue.runCatching { this.toDouble() }
                    .onSuccess { graphEdge.weight = it }
                    .onFailure { graphEdge.weight = 0.0 }
                edge.textField.text = graphEdge.weight.toString()
            }
        }
    }

    private val primButton : Button by lazy {
        Button(cp5, "prim-button")
            .setCaptionLabel("Prim's Algorithmus")
            .setFont(createFont("Consolas", 12f))
            .setPosition(0f, 0f)
            .setWidth(150)
            .setHeight(20)
            .onClick { println("clicked Prim") }
    }

    private val kruskalButton : Button by lazy {
        Button(cp5, "kruskal-button")
            .setCaptionLabel("Kruskal's Algorithmus")
            .setFont(createFont("Consolas", 12f))
            .setPosition(155f, 0f)
            .setWidth(150)
            .setHeight(20)
            .onClick { println("clicked Kruskal") }
    }

    private val dropDownList : DropdownList by lazy {
        DropdownList(cp5, "saved-graphs")
            .setPosition(310f, 0f)
            .setWidth(150)
            .setHeight(20)
            .setBarHeight(20)
            .setItemHeight(20)
    }

    fun solve(strategy: StepwiseMST)
    {
    }

    private fun loadSavedGraphs()
    {

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

        primButton
        kruskalButton
        dropDownList

        loadSavedGraphs()
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
        nodeList.forEach { it.drawPadding(this) }

        edgeSet.forEach { edge ->
            val a = PVector(edge.first.x, edge.first.y)
            val b = PVector(edge.second.x, edge.second.y)
            val aToB = b.sub(a).setMag(edge.first.radius)

            val x1 = edge.first.x + aToB.x
            val y1 = edge.first.y + aToB.y
            val x2 = edge.second.x - aToB.x
            val y2 = edge.second.y - aToB.y

            stroke(config.node.strokeColor)
            line(x1, y1, x2, y2)

            noStroke()
            val textField = edge.textField
            val firstVector = PVector(x1, y1)
            val secondVector = PVector(x2, y2)
            val firstToSecond = secondVector.sub(firstVector)
            firstToSecond.setMag(firstToSecond.mag() * edge.textFieldOffsetRatio)

            val tx = x1 + firstToSecond.x - textField.width / 2
            val ty = y1 + firstToSecond.y - textField.height / 2

            textField.setPosition(tx, ty)
        }

        strokeWeight(config.node.strokeWeight)

        stroke(config.node.strokeColor)
        fill(config.node.fillColor)
        nodeList.forEach { it.draw(this) }

        highlightedNode?.let {
            stroke(config.node.strokeColor)
            fill(config.node.highlightedFillColor)
            it.draw(this)
        }

        selectedNode?.let {
            stroke(config.node.strokeColor)
            fill(config.node.selectedFillColor)
            it.draw(this)
        }

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

    private var nodeIdCounter = 1

    inner class Node(var x: Float, var y: Float,
                     radius: Float, padding: Float)
    {
        var radius: Float = radius
            set(value) { field = abs(value) }

        var padding: Float = padding
            set (value) { field = abs(value) }

        val realPadding
            get() = this.padding + config.node.strokeWeight

        val diameter: Float
            get() = radius * 2

        val id: Int by lazy { nodeIdCounter++ }

        fun draw(context: PApplet)
        {
            context.ellipse(x, y, diameter, diameter)
        }

        fun drawPadding(context: PApplet)
        {
            val d = radius + padding
            context.ellipse(x, y, d * 2, d * 2)
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

    private var edgeIdCounter = 1

    inner class Edge(val between: Pair<Node, Node>)
    {
        val first: Node get() = between.first
        val second: Node get() = between.second

        val textField : Textfield by lazy {
            val textField = Textfield(cp5, "textfield-${first.id}-${second.id}")
                .setFont(createFont("Consolas", 14f))
                .setSize(50, 20)
                .setAutoClear(false)
                .setCaptionLabel("")
                .setColorBackground(color(255, 230))
                .setColorForeground(color(255, 230))
                .setColorValue(color(0))
                .setColorActive(color(200))
                .setColorCursor(color(0))
                .setId(edgeIdCounter++)
            textField
        }

        var textFieldOffsetRatio : Float = 0.5f

        fun hasNode(node: Node) = first == node || second == node

        override fun hashCode() = first.hashCode() + second.hashCode()
        override fun equals(other: Any?) : Boolean
        {
            if (this === other) return true
            if (other !is Edge) return false

            if (first == other.first && second == other.second) return true
            if (first == other.second && second == other.first) return true

            return false
        }
    }

    private val graph = Graph()
    private val nodeMap = mutableMapOf<Int, Node>()
    private val nodeList = mutableListOf<Node>()
    private val edgeSet = mutableSetOf<Edge>()
    private val edgeMap = mutableMapOf<Int, Edge>()

    private val cp5: ControlP5 by lazy { ControlP5(this) }

    private var createdNode: Node? = null
    private var clickedNode: Node? = null
    private var selectedNode: Node? = null
    private var highlightedNode: Node? = null
    private var clickedTextfieldEdge: Edge? = null

    private var clickedX: Float = 0f
    private var clickedY: Float = 0f
    private var clickedOffsetX: Float = 0f
    private var clickedOffsetY: Float = 0f

    private var isScrolling: Boolean = false
    private var isDragging: Boolean = false
    private var isDraggingTextfield: Boolean = false

    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

    override fun mousePressed()
    {
        super.mousePressed()

        // TODO: holding SHIFT while connecting nodes should
        //  automatically select the node that was clicked on last.

        // TODO: while connecting nodes, left-clicking on an empty spot
        //  should not create a new node.

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


        // Do not pass click events through if a text field is pressed.
        for (edge in edgeSet) {
            if (edge.textField.isMousePressed) {
                println("clicked an edge..")
                clickedTextfieldEdge = edge
            }
            else if (edge.textField.isFocus)
                // Submit the text field when the user clicks outside
                // of it and doesn't press enter like it's expected.
                edge.textField.submit()
        }

        if (clickedTextfieldEdge != null)
            return

        // Same goes with any button.
        if (primButton.isPressed) return
        if (kruskalButton.isPressed) return
        if (dropDownList.isMousePressed) return

        val x = mouseX.toFloat()
        val y = mouseY.toFloat()

        val node = Node(x, y, config.node.radius, config.node.padding)

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

    private fun edgeLineBetween(node1: Node, node2: Node)
        : Pair<PVector, PVector>
    {
        val a = PVector(node1.x, node1.y)
        val b = PVector(node2.x, node2.y)
        val aToB = PVector.sub(b, a)

        aToB.setMag(node1.radius)
        val start = PVector(node1.x + aToB.x, node1.y + aToB.y)

        aToB.setMag(node2.radius)
        val end = PVector(node2.x - aToB.x, node2.y - aToB.y)

        return start to end
    }

    override fun mouseDragged()
    {
        super.mouseDragged()

        clickedTextfieldEdge?.let { edge ->
            if (!isDraggingTextfield) {
                isDraggingTextfield = true
                edge.textField.submit()
            }

            val (lineStart, lineEnd) = edgeLineBetween(edge.first, edge.second)
            val projectedPoint = project(
                PVector(mouseX.toFloat(), mouseY.toFloat()),
                lineStart, lineEnd,
                true
            )

            val distance = PVector.sub(projectedPoint, lineStart)
            val fullDistance = PVector.sub(lineEnd, lineStart)
            edge.textFieldOffsetRatio = distance.mag() / fullDistance.mag()
        } ?: clickedNode?.also { node ->
            if (mouseButton != LEFT)
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
            if (mouseButton != LEFT)
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
        super.mouseReleased()

        clickedNode?.takeIf {
            // We're not selecting a node if it was dragged around.
            // Also, only select nodes with the right mouse button.
            !isDragging
        } ?.let { clicked ->
            if (mouseButton == RIGHT) {
                // Remove all references to the node.
                nodeList.remove(clicked)
                if (highlightedNode == clicked) highlightedNode = null
                if (selectedNode == clicked) selectedNode = null

                graph.removeVertex(clicked.id)

                // Remove all edges containing this node.
                val iterator = edgeSet.iterator()
                for (edge in iterator)
                    if (edge.hasNode(clicked)) {
                        edgeMap.remove(edge.textField.id)
                        iterator.remove()
                        cp5.remove(edge.textField.name)
                    }

                return@let
            }

            selectedNode?.takeIf {
                // Only add an edge if the two nodes are different.
                it != clicked
            } ?.also { selected ->
                val edge = Edge(selected to clicked)
                when (edge in edgeSet) {
                    true -> {
                        // TODO: Improve this.
                        val actualEdge = edgeSet.find { it == edge } !!

                        cp5.remove(actualEdge.textField.name)
                        edgeSet.remove(actualEdge)
                        graph.removeEdge(actualEdge.first.id to actualEdge.second.id)
                    }
                    false -> {
                        edgeSet.add(edge)
                        edge.textField.isFocus = true
                        graph.addUndirectedEdge(edge.first.id, edge.second.id)
                        edgeMap[edge.textField.id] = edge
                    }
                }

                selectedNode = null // Edge added, selection done.
            } ?: run {
                // Either we selected a different node,
                // or the node was deselected.
                selectedNode = clicked.takeIf { it != selectedNode }
            }
        }

        createdNode?.let {
            nodeList.add(it)
            highlightedNode = it

            graph.addVertex(it.id)
            nodeMap[it.id] = it
        }
        createdNode = null
        clickedNode = null
        clickedTextfieldEdge = null

        isDragging = false
        isScrolling = false
        isDraggingTextfield = false

        clickedOffsetX = 0f
        clickedOffsetY = 0f
    }

    override fun mouseMoved()
    {
        val x = mouseX.toFloat()
        val y = mouseY.toFloat()

        highlightedNode = nodeList.firstOrNull { it.contains(x, y) }
    }

    private fun scrollBy(dx: Int, dy: Int)
    {
        nodeList.forEach { node ->
            node.x += dx
            node.y += dy
        }
    }

    /* === external stuff === */

    // https://www.openprocessing.org/sketch/107041/
    fun project(p: PVector, a: PVector, b: PVector, asSegment: Boolean)
            : PVector
    {
        val A = p.x - a.x
        val B = p.y - a.y
        val C = b.x - a.x
        val D = b.y - a.y

        val dot = A * C + B * D
        val len = C * C + D * D
        val t = dot / len

        if (asSegment) {
            if (t < 0) return a
            if (t > 1) return b
        }
        return PVector(a.x + t * C, a.y + t * D)

    }
}
