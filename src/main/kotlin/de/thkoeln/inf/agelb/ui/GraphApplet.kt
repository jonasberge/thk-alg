package de.thkoeln.inf.agelb.ui

import de.thkoeln.inf.agelb.adt.graph.Graph
import processing.core.PApplet
import processing.core.PVector
import kotlin.math.pow
import controlP5.*
import controlP5.Textfield
import controlP5.ControlEvent
import de.thkoeln.inf.agelb.mst.*
import controlP5.Textlabel
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import java.io.File
import kotlin.collections.HashMap
import kotlin.math.roundToInt

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
        // println(event.controller.name)
        if (event.isController && event.controller.name == stateNameTextField.name) {
            addStateNamePreview()
            stateNameTextField.isFocus = false
            return
        }

        if (event.isController && event.controller.name == stateSelectDropdown.name) {
            val state = getSelectedGraphState() ?: return
            removeStateNamePreview()
            stateNameTextField.text = state.name
            stateNameTextField.stringValue = state.name
            return
        }

        edgeTextFieldMap[event.id]?.let { edge ->
            edge.textField!!.isFocus = false
            graph.getEdge(edge.first.id to edge.second.id)?.let { graphEdge ->
                event.stringValue.replace(',', '.')
                    .runCatching { this.toDouble() }
                    .onSuccess { graphEdge.weight = it }
                    .onFailure { graphEdge.weight = 0.0 }
                edge.textField!!.text = graphEdge.weight.toString()
            }
        }
    }

    private lateinit var primButton : Button
    private lateinit var kruskalButton : Button

    private var solverCurrentStep = 0
    private var isSolverDone: Boolean = false

    private var buttonIdCounter: Int = 1
    private val buttonToListener = mutableMapOf<Button, () -> Unit>()
    private fun createButton(title: String, x: Float, y: Float, w: Int, h: Int,
                             onClick: () -> Unit) : Button
    {
        val button = Button(cp5, "button-${buttonIdCounter++}")
            .setCaptionLabel(title)
            .setFont(createFont("Consolas", 12f))
            .setPosition(x, y)
            .setWidth(w)
            .setHeight(h)
            .onClick { onClick() }
        buttonToListener[button] = onClick
        return button
    }

    private lateinit var informationTextLabel: Textlabel

    private lateinit var nextStepButton: Button
    private lateinit var previousStepButton: Button
    private lateinit var cancelSolveButton: Button

    private lateinit var saveStateButton: Button
    private lateinit var loadStateButton: Button
    private lateinit var deleteStateButton: Button
    private lateinit var stateNameTextField: Textfield
    private lateinit var stateCommentTextLabel: Textlabel
    private lateinit var stateSelectDropdown: DropdownList
    private lateinit var checkBoxIsChangeable: CheckBox

    private var isMstComplete: Boolean = false
    private var isSolvingWithPrim: Boolean = false
    private var isSolvingWithKruskal: Boolean = false
    private var isSolving: Boolean
        get() = isSolvingWithPrim || isSolvingWithKruskal
        set(value) {
            require(!value) { "Can't solve with both algorithms" }
            isSolvingWithPrim = value
            isSolvingWithKruskal = value
        }

    private var primRootNode: Node? = null
    private var selectedNodeHistory: Stack<Node> = Stack()
    private val selectedEdges: MutableSet<Edge> = mutableSetOf()
    private var solutionSteps: List<MSTStep>? = null

    private fun updateStepDescriptionPrim(step: PrimStepwiseMST.Step)
    {
        when (step.type) {
            PrimStepwiseMST.StepType.NEIGHBOR_INSPECT -> {
                informationTextLabel.setText("Untersuche Nachbarn"
                        + " des zuletzt ausgewählten Knotens.")
            }
            PrimStepwiseMST.StepType.NODE_SELECT -> {
                informationTextLabel.setText("Füge den Knoten dem Baum"
                        + " hinzu, welcher über eine Kante erreichbar ist"
                        + " welche das geringfügigste Gewicht hat.")
            }
            else -> { }
        }
    }

    private fun revertStepPrim(step: PrimStepwiseMST.Step)
    {
        when (step.type) {
            PrimStepwiseMST.StepType.NODE_SELECT -> {
                selectedNodeHistory.pop()
                selectedEdges.remove(edgeMap[step.parentNode!! to step.node!!]!!)
            }
            else -> { }
        }
    }

    private fun applyStepPrim(step: PrimStepwiseMST.Step)
    {
        updateStepDescriptionPrim(step)

        when (step.type) {
            PrimStepwiseMST.StepType.NODE_SELECT -> {
                selectedNodeHistory.push(nodeMap[step.node]!!)
                selectedEdges.add(edgeMap[step.parentNode!! to step.node!!]!!)
            }
            else -> { }
        }
    }

    private fun updateStepDescriptionKruskal(step: KruskalStepwiseMST.Step)
    {
        // TODO: Proper descriptions.
        when (step.type) {
            KruskalStepwiseMST.StepType.EDGE_CYCLES -> {
                informationTextLabel.setText("EDGE_CYCLES")
            }
            KruskalStepwiseMST.StepType.EDGE_SELECT -> {
                informationTextLabel.setText("EDGE_SELECT")
            }
            KruskalStepwiseMST.StepType.EDGE_INSPECT -> {
                informationTextLabel.setText("EDGE_INSPECT")
            }
        }
    }

    private val inspectedEdgeHistory: Stack<Edge> = Stack()

    private fun revertStepKruskal(step: KruskalStepwiseMST.Step)
    {
        when (step.type) {
            KruskalStepwiseMST.StepType.EDGE_CYCLES -> {
                inspectedEdgeHistory.pop()
            }
            KruskalStepwiseMST.StepType.EDGE_INSPECT -> {
                inspectedEdgeHistory.pop()
            }
            KruskalStepwiseMST.StepType.EDGE_SELECT -> {
                val edge = edgeMap[step.edge.from to step.edge.to]!!
                selectedEdges.remove(edge)
            }
        }
    }

    private fun applyStepKruskal(step: KruskalStepwiseMST.Step)
    {
        updateStepDescriptionKruskal(step)

        val edge = edgeMap[step.edge.from to step.edge.to]!!

        when (step.type) {
            KruskalStepwiseMST.StepType.EDGE_CYCLES -> {
                inspectedEdgeHistory.push(edge)
            }
            KruskalStepwiseMST.StepType.EDGE_INSPECT -> {
                inspectedEdgeHistory.push(edge)
            }
            KruskalStepwiseMST.StepType.EDGE_SELECT -> {
                selectedEdges.add(edge)
            }
        }
    }

    private val stateDropDownDefaultValue = "Graph auswählen..."
    private val stateNamePreviewValue = "Name des Graphen"
    private val stateNamePreviewColor = color(140)
    private var isStateNamePreviewed = false

    private fun addStateNamePreview()
    {
        if (stateNameTextField.text.isNotEmpty())
            return

        stateNameTextField.text = stateNamePreviewValue
        stateNameTextField.setColorValue(stateNamePreviewColor)
        isStateNamePreviewed = true
    }

    private fun removeStateNamePreview()
    {
        if (stateNameTextField.text != stateNamePreviewValue)
            return

        stateNameTextField.text = ""
        stateNameTextField.setColorValue(color(0))
        isStateNamePreviewed = false
    }

    private val loadedGraphStates = mutableSetOf<GraphState>()

    private fun getSelectedGraphState() : GraphState?
    {
        val index = stateSelectDropdown.value.roundToInt()
        if (index >= stateSelectDropdown.items.size || index < 0)
            return null

        // This is more than weird .. but it works.
        val hashMap = stateSelectDropdown.items[index] as HashMap<String, Any?>
        return hashMap["value"] as GraphState
    }

    private fun getResourceAsText(path: String)
            = object {}.javaClass.getResource(path).readText()

    private val isDebugging: Boolean by lazy {
        getResourceAsText("/is_debugging").firstOrNull()?.equals('1') ?: false
    }

    private val isLockedChecked: Boolean
        get() = isDebugging && checkBoxIsChangeable.getItem(0).booleanValue

    override fun setup()
    {
        surface.setResizable(config.isResizable)
        surface.setSize(config.width, config.height)

        val topLeftX = displayWidth / 2 - config.width / 2
        val topLeftY = displayHeight / 2 - config.height / 2
        surface.setLocation(topLeftX, topLeftY)

        previousWidth = width
        previousHeight = height

        Label.setUpperCaseDefault(false)

        informationTextLabel = Textlabel(cp5, "", 155, 0, 150, 20)
            .setFont(createFont("Consolas", 12f))
            .setColor(color(0))

        cancelSolveButton = createButton("- Abbrechen -", 0f, 0f, 150, 20) {
            nextStepButton.hide()
            previousStepButton.hide()
            cancelSolveButton.hide()
            primButton.show()
            kruskalButton.show()
            informationTextLabel.hide()
            informationTextLabel.setText("")
            loadStateButton.show()
            stateNameTextField.show()
            saveStateButton.show()
            deleteStateButton.show()
            stateSelectDropdown.show()
            isMstComplete = false
            isSolving = false
            isSolverDone = false
            selectedEdges.clear()
            primRootNode = null
            selectedNodeHistory.clear()
            solutionSteps = null
            solverCurrentStep = 0
        }

        nextStepButton = createButton("Nächster Schritt", 0f, 0f, 150, 20) {
            solutionSteps?.let { steps ->
                if (solverCurrentStep < steps.size) {
                    solverCurrentStep++
                    val step = steps[solverCurrentStep - 1]
                    if (isSolvingWithPrim)
                        applyStepPrim(step as PrimStepwiseMST.Step)
                    else if (isSolvingWithKruskal)
                        applyStepKruskal(step as KruskalStepwiseMST.Step)
                }
                else {
                    isSolverDone = true
                    val text: String = if (isMstComplete)
                        "Lösung gefunden - Der minimal-spannende" +
                                " Baum ist vollständig, d.h. alle Knoten" +
                                " sind enthalten."
                    else
                        "Lösung gefunden - Der minimal-spannende" +
                                " Baum ist nicht vollständig, d.h. dass" +
                                " einige Knoten nicht enthalten sind."
                    informationTextLabel.setText(text)
                }
                Unit
            }
        }

        previousStepButton = createButton("Vorheriger Schritt", 0f, 0f, 150, 20) {
            solutionSteps?.let { steps ->
                // TODO: Differentiate between Prim and Kruskal.
                if (isSolverDone) {
                    isSolverDone = false
                    return@let
                }
                if (solverCurrentStep > 0) {
                    if (isSolvingWithPrim)
                        revertStepPrim(steps[solverCurrentStep - 1]
                                as PrimStepwiseMST.Step)
                    else if (isSolvingWithKruskal)
                        revertStepKruskal(steps[solverCurrentStep - 1]
                                as KruskalStepwiseMST.Step)
                    solverCurrentStep--

                    if (solverCurrentStep > 0) {
                        val step = steps[solverCurrentStep - 1]
                        if (isSolvingWithPrim)
                            updateStepDescriptionPrim(step as PrimStepwiseMST.Step)
                        else if (isSolvingWithKruskal)
                            updateStepDescriptionKruskal(step as KruskalStepwiseMST.Step)
                    }
                }
                if (isSolvingWithPrim && solverCurrentStep == 0)
                    informationTextLabel.setText("Startknoten wurde ausgewählt.")
            }
        }

        primButton = createButton("Prim's Algorithmus", 0f, 0f, 150, 20) {
            isSolvingWithPrim = true
            selectedNode = null // Deselect any selected node.

            informationTextLabel.setText("Wähle einen Startknoten...")

            primButton.hide()
            kruskalButton.hide()
            stateSelectDropdown.hide()
            cancelSolveButton.show()
            informationTextLabel.show()

            stateCommentTextLabel.setText("")
            stateNameTextField.hide()
            loadStateButton.hide()
            saveStateButton.hide()
            deleteStateButton.hide()
        }

        kruskalButton = createButton("Kruskal's Algorithmus", 155f, 0f, 150, 20) {
            isSolvingWithKruskal = true
            selectedNode = null // Deselect any selected node.

            primButton.hide()
            kruskalButton.hide()
            cancelSolveButton.show()
            informationTextLabel.show()

            stateCommentTextLabel.setText("")
            stateSelectDropdown.hide()
            stateNameTextField.hide()
            loadStateButton.hide()
            saveStateButton.hide()
            deleteStateButton.hide()

            nextStepButton.show()
            previousStepButton.show()

            val solver = KruskalStepwiseMST(graph)
            solutionSteps = solver.steps().toList()
            isMstComplete = solver.complete
        }

        saveStateButton = createButton("Graph speichern", 155f, 0f, 150, 20) {
            stateCommentTextLabel.setText("")

            if (isStateNamePreviewed) {
                stateCommentTextLabel.setText("Speichern fehlgeschlagen." +
                        " Gebe einen Namen für den Graphen ein.")
                return@createButton
            }

            val name = stateNameTextField.stringValue
            val time = System.currentTimeMillis()
            val state = createGraphState(name, "graphs/$time.ser", isLockedChecked)

            loadedGraphStates.firstOrNull { it.name == name } ?.let { existingState ->
                if (!isDebugging && existingState.isLocked) {
                    stateCommentTextLabel.setText("Speichern fehlgeschlagen." +
                            " Dieser Graph kann nicht überschrieben werden.")
                    return@createButton
                }

                state.fileName = existingState.fileName
                loadedGraphStates.remove(existingState)
                stateSelectDropdown.getItem(existingState.name)["value"] = state
            } ?: let {
                val value = stateSelectDropdown.items.size.toFloat()
                stateSelectDropdown.addItem(state.name, state)
                stateSelectDropdown.value = value
                stateSelectDropdown.setLabel(state.name)
            }

            saveGraphState(state)
            loadedGraphStates.add(state)
        }

        deleteStateButton = createButton("Löschen", 0f, 0f, 72, 20) {
            stateCommentTextLabel.setText("")

            // TODO: Move delete currently displayed graph to a different button.
            // resetState()

            val state = getSelectedGraphState() ?: return@createButton

            if (!isDebugging && state.isLocked) {
                stateCommentTextLabel.setText("Speichern fehlgeschlagen." +
                        " Dieser Graph kann nicht gelöscht werden.")
                return@createButton
            }

            File(state.fileName).takeIf { it.exists() } ?.delete()

            loadedGraphStates.remove(state)
            stateSelectDropdown.removeItem(state.name)
            stateSelectDropdown.label = stateDropDownDefaultValue

            stateNameTextField.clear()
            addStateNamePreview()
        }

        loadStateButton = createButton("Laden", 78f, 0f, 72, 20) {
            stateCommentTextLabel.setText("")
            val state = getSelectedGraphState() ?: return@createButton
            applyGraphState(state)
        }

        checkBoxIsChangeable = cp5.addCheckBox("overwriteable-checkbox")
            .setPosition(310f, 29f)
            .setSize(12, 12)
            .setColorLabel(color(0))
            .setFont(createFont("Consolas", 12f)) // doesn't work ?!
            .addItem("lock - not deletable in non-debug mode", 0f)

        if (!isDebugging) {
            checkBoxIsChangeable.hide()
        }

        stateCommentTextLabel = cp5.addTextlabel("state-comment", "", 310, 0)
            .setFont(createFont("Consolas", 12f))
            .setColor(color(0))

        stateSelectDropdown = cp5.addDropdownList(stateDropDownDefaultValue)
            .setFont(createFont("Consolas", 12f))
            .setPosition(0f, 25f)
            .setBackgroundColor(color(190))
            .setWidth(150)
            .setItemHeight(20)
            .setBarHeight(20)
            .setBackgroundColor(color(60))
            .setColorActive(color(255, 128))

        stateSelectDropdown.captionLabel.style.marginTop = 3
        stateSelectDropdown.captionLabel.style.marginLeft = 3

        loadedGraphStates.addAll(readAllGraphStates("graphs/"))
        for (state in loadedGraphStates) {
            stateSelectDropdown.addItem(state.name, state)
        }

        // println(stateSelectDropdown.value)

        stateSelectDropdown.isOpen = false

        stateNameTextField = createTextField("state-name")
        stateNameTextField.setPosition(155f, 25f)
        stateNameTextField.setSize(150, 20)
        addStateNamePreview()

        updateButtonPositions()

        nextStepButton.hide()
        previousStepButton.hide()
        cancelSolveButton.hide()
        informationTextLabel.hide()

    }

    private fun updateButtonPositions()
    {
        primButton.position[1] = height - 20f
        kruskalButton.position[1] = height - 20f

        informationTextLabel.position[1] = height - 20f

        nextStepButton.position[1] = height - 50f - 20f
        previousStepButton.position[1] = height - 25f - 20f
        cancelSolveButton.position[1] = height - 20f
    }

    override fun draw()
    {
        if (width != previousWidth || height != previousHeight) {
            val dx = (width - previousWidth) / 2
            val dy = (height - previousHeight) / 2
            scrollBy(dx, dy)
            previousWidth = width
            previousHeight = height

            updateButtonPositions()

            // https://github.com/sojamo/controlp5/issues/26
            // One cannot interact with ControlP5 elements within the
            // space that is added when resizing the applet window.
            cp5.setGraphics(this, 0, 0)
        }

        clear()
        background(config.backgroundColor)

        noStroke()
        fill(config.node.paddingColor)
        nodeList.forEach { it.drawPadding(this) }

        edgeSet.forEach { edge ->
            val (start, end) = edgeLineBetween(edge.first, edge.second)

            stroke(config.node.strokeColor)
            if (isSolvingWithPrim && primRootNode != null || isSolvingWithKruskal)
                stroke(color(0, 30))
            line(start.x, start.y, end.x, end.y)

            noStroke()
            val textField = edge.textField!!
            val firstVector = PVector(start.x, start.y)
            val secondVector = PVector(end.x, end.y)
            val firstToSecond = secondVector.sub(firstVector)
            firstToSecond.setMag(firstToSecond.mag() * edge.textFieldOffsetRatio)

            val tx = start.x + firstToSecond.x - textField.width / 2
            val ty = start.y + firstToSecond.y - textField.height / 2

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

        if (informationTextLabel.isVisible)
            informationTextLabel.draw(this)

        // === ... === //

        val rootNodeBlue = color(50, 80, 180)
        val lastSelectedNodeBlue = color(120, 180, 255)
        val otherNodeBlue = color(80, 120, 220)

        val lastSelectedNodeTokenAqua = color(200, 250, 215)
        val neighbourNodeTokenAqua = color(lastSelectedNodeTokenAqua, 200)
        val leastWeightEdgeAqua = color(160, 220, 195)

        selectedEdges.forEach { edge ->
            stroke(config.node.strokeColor)
            fill(otherNodeBlue)
            edge.first.draw(this)
            edge.second.draw(this)

            stroke(rootNodeBlue)
            val (start, end) = edgeLineBetween(edge.first, edge.second)
            line(start.x, start.y, end.x, end.y)
        }

        // === PRIM === //

        primRootNode?.let {
            stroke(config.node.strokeColor)
            fill(rootNodeBlue)
            it.draw(this)
        }

        solutionSteps?.takeIf {
            isSolvingWithPrim && solverCurrentStep > 0
        } ?.let { steps ->
            if (isSolverDone)
                return@let

            val step = steps[solverCurrentStep - 1] as PrimStepwiseMST.Step

            stroke(0)
            step.queue.forEach { edge ->
                val (start, end) = edgeLineBetween(nodeMap[edge.from]!!, nodeMap[edge.to]!!)
                line(start.x, start.y, end.x, end.y)
            }

            // The edge with the least weight is stored first.
            stroke(leastWeightEdgeAqua)
            step.queue.firstOrNull()?.let { edge ->
                val (start, end) = edgeLineBetween(nodeMap[edge.from]!!, nodeMap[edge.to]!!)
                line(start.x, start.y, end.x, end.y)
            }

            stroke(config.node.strokeColor)
            when (step.type) {
                PrimStepwiseMST.StepType.NEIGHBOR_INSPECT -> {
                    fill(neighbourNodeTokenAqua)
                    val node = nodeMap[step.node]!!
                    ellipse(node.x, node.y, node.radius * 0.5f, node.radius * 0.5f)
                }
                else -> { }
            }
        }

        // === KRUSKAL === //

        solutionSteps?.takeIf {
            isSolvingWithKruskal && solverCurrentStep > 0
        } ?.let { steps ->
            if (isSolverDone)
                return@let

            val step = steps[solverCurrentStep - 1] as KruskalStepwiseMST.Step

            val isInspecting = step.type == KruskalStepwiseMST.StepType.EDGE_INSPECT
            val isCycling = step.type == KruskalStepwiseMST.StepType.EDGE_CYCLES
            if (inspectedEdgeHistory.isNotEmpty() && (isInspecting || isCycling))
                inspectedEdgeHistory.peek().let { edge ->
                    stroke(config.node.strokeColor)
                    fill(lastSelectedNodeBlue)
                    edge.first.draw(this)
                    edge.second.draw(this)

                    val (start, end) = edgeLineBetween(edge.first, edge.second)

                    stroke(if (isInspecting) lastSelectedNodeBlue else color(240, 40, 20))
                    line(start.x, start.y, end.x, end.y)
                }
        }


        // === ... === //

        if (selectedNodeHistory.isNotEmpty() && !isSolverDone)
            selectedNodeHistory.peek().let { node ->
                stroke(config.node.strokeColor)
                fill(lastSelectedNodeBlue)
                node.draw(this)

                fill(lastSelectedNodeTokenAqua)
                ellipse(node.x, node.y, node.radius, node.radius)
            }
    }

    private var nodeIdCounter = 1

    private fun createNode(x: Float, y: Float) : Node
    {
        return Node(x, y, config.node.radius, config.node.padding,
            config.node.strokeWeight, nodeIdCounter++)
    }

    private fun updateNode(node: Node)
    {
        node.radius = config.node.radius
        node.padding = config.node.padding
        node.strokeWeight = config.node.strokeWeight
    }

    @Serializable
    data class Node(var x: Float, var y: Float,
                    private var _radius: Float,
                    private var _padding: Float,
                    var strokeWeight: Float,
                    var id: Int)
    {
        var radius: Float
            get() = _radius
            set(value) { _radius = abs(value) }

        var padding: Float
            get() = _padding
            set(value) { _padding = abs(value) }

        val realPadding
            get() = this.padding + strokeWeight

        val diameter: Float
            get() = radius * 2

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

    private fun createTextField(name: String = "") : Textfield
    {
        val isNameSupplied = name.isNotEmpty() && name.isNotBlank()

        var id = 0
        var theName = name
        if (!isNameSupplied) {
            id = edgeIdCounter++
            theName = "textfield-$id"
        }

        val textField = Textfield(cp5, theName)
            .setFont(createFont("Consolas", 14f))
            .setSize(50, 20)
            .setAutoClear(false)
            .setCaptionLabel("")
            .setColorBackground(color(255, 205))
            .setColorForeground(color(255, 205))
            .setColorValue(color(0))
            .setColorActive(color(200))
            .setColorCursor(color(0))

        if (!isNameSupplied)
            textField.id = id

        return textField
    }

    @Serializable
    class Edge(val between: Pair<Node, Node>)
    {
        val first: Node get() = between.first
        val second: Node get() = between.second

        @kotlinx.serialization.Transient
        var textField : Textfield? = null

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

    /*
    Graphstate stuff in here
     */

    @Serializable
    class GraphState(val name: String,
                     var fileName: String,
                     val isLocked: Boolean,
                     val nodeList: List<Node>,
                     val nodeMap: Map<Int, Int>,
                     val edgeList: List<GraphState.Edge>)
    {
        @Serializable
        class Edge(val first: Int, val second: Int, val weight: Double,
                   val textFieldOffsetRatio: Float)
    }

    private fun createGraphState(name: String, fileName: String,
                                 isLocked: Boolean) : GraphState
    {
        val theNodeList = nodeList.map { it.copy() } // index -> node
        val nodeToIndex = mutableMapOf<Node, Int>() // node -> node index
        theNodeList.mapIndexed { index, node -> nodeToIndex[node] = index }

        val theNodeMap = mutableMapOf<Int, Int>() // node id -> node index
        nodeMap.forEach { (id, node) -> theNodeMap[id] = nodeToIndex[node]!! }

        // edge -> (node index) to (node index)
        val edgeToNodeIndexPair = mutableMapOf<Edge, Pair<Int, Int>>()
        edgeSet.forEach { edge ->
            val pair = nodeToIndex[edge.first]!! to nodeToIndex[edge.second]!!
            edgeToNodeIndexPair[edge] = pair
        }

        // index -> state edge (node index, node index, weight)
        val theEdgeList = mutableListOf<GraphState.Edge>()
        edgeSet.forEach { edge ->
            val graphEdge = graph.getEdge(edge.first.id to edge.second.id)!!
            val indexPair = edgeToNodeIndexPair[edge]!!
            theEdgeList.add(GraphState.Edge(indexPair.first,
                indexPair.second, graphEdge.weight, edge.textFieldOffsetRatio))
        }

        return GraphState(name, fileName, isLocked,
            theNodeList, theNodeMap.toMap(), theEdgeList)
    }

    private fun resetState()
    {
        edgeTextFieldMap.clear()
        for (edge in edgeSet)
            cp5.remove(edge.textField!!.name)

        nodeList.clear()
        nodeMap.clear()

        edgeSet.clear()
        edgeMap.clear()

        graph.clear()

        edgeIdCounter = 1
        nodeIdCounter = 1
    }

    private fun applyGraphState(state: GraphState)
    {
        resetState()

        // The node id counter needs to be larger than any existing id
        // of the loaded nodes. Otherwise, if it would be reset to 1,
        // we could collide with existing nodes when creating one.
        for (node in state.nodeList)
            nodeIdCounter = max(nodeIdCounter, node.id)
        nodeIdCounter += 1

        // Add all nodes.
        // nodeList.addAll(state.nodeList)
        for (node in state.nodeList) {
            nodeList.add(node.copy())
            graph.addVertex(node.id)
        }
        for ((id, index) in state.nodeMap)
            nodeMap[id] = nodeList[index]

        // Recreate all edges.
        for (stateEdge in state.edgeList) {
            val first = nodeList[stateEdge.first]
            val second = nodeList[stateEdge.second]
            val edge = Edge(first to second)

            edge.textFieldOffsetRatio = stateEdge.textFieldOffsetRatio

            edge.textField = createTextField()
            edge.textField!!.text = stateEdge.weight.toString()
            edgeTextFieldMap[edge.textField!!.id] = edge

            edgeSet.add(edge)
            edgeMap[first.id to second.id] = edge
            edgeMap[second.id to first.id] = edge

            graph.addUndirectedEdge(first.id, second.id, stateEdge.weight)
        }

        for (node in nodeList)
            updateNode(node)

        // scrollBy

        if (nodeList.size == 0)
            return

        var minX = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY

        for (node in nodeList) {
            minX = min(minX, node.x)
            maxX = max(maxX, node.x)
            minY = min(minY, node.y)
            maxY = max(maxY, node.y)
        }

        val w = maxX - minX
        val h = maxY - minY

        val x = (width - w) / 2
        val y = (height - h) / 2

        val xOffset = x - minX
        val yOffset = y - minY

        scrollBy(xOffset, yOffset)
    }

    private fun saveGraphState(state: GraphState)
    {
        val data = Cbor().dump(GraphState.serializer(), state)
        File(state.fileName)
            .also {
                if (!it.exists()) {
                    it.parentFile.mkdirs()
                    it.createNewFile()
                }
            }
            .writeBytes(data)
    }

    private fun readGraphState(fileName: String) : GraphState?
    {
        val file = File(fileName)
        if (!file.exists())
            return null

        var state: GraphState? = null
        try {
            state = Cbor().load(GraphState.serializer(), file.readBytes())
            state.fileName = fileName
        }
        catch (e: Exception) {
            println("Failed to load graph file '$fileName': Invalid format.")
            val success = file.renameTo(File(file.absolutePath + ".invalid"))
        }

        return state
    }

    private fun readAllGraphStates(directoryName: String) : List<GraphState>
    {
        val states = mutableListOf<GraphState>()

        val dir = File(directoryName)
        dir.walk().forEach { file ->
            if (file.extension == "ser")
                readGraphState(file.absolutePath)?.let { states.add(it) }
        }

        return states.toList()
    }

    /* == ... == */

    private var graph = Graph()
    private val nodeMap = mutableMapOf<Int, Node>()
    private var nodeList = mutableListOf<Node>()
    private var edgeSet = mutableSetOf<Edge>()
    // Maps Textfield IDs to its Edge.
    private var edgeTextFieldMap = mutableMapOf<Int, Edge>()
    // Maps a pair of Nodes to their edge.
    private var edgeMap = mutableMapOf<Pair<Int, Int>, Edge>()

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
    private var hasMouseDragged: Boolean = false

    private var clickedButton: Button? = null

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

        // TODO: shift-drag rotates around initial click-point.

        // TODO: TAB and SHIFT-TAB to switch between labels.

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


        if (stateNameTextField.isMousePressed) {
            stateCommentTextLabel.setText("")
            removeStateNamePreview()
            return
        }
        else if (stateNameTextField.isFocus) {
            stateNameTextField.submit()
            return
        }

        val bx = checkBoxIsChangeable.position[0]
        val by = checkBoxIsChangeable.position[1]
        val w = checkBoxIsChangeable.width.toFloat()
        val h = checkBoxIsChangeable.height.toFloat()
        if (boxContains(bx, by, w, h, mouseX.toFloat(), mouseY.toFloat()))
            return

        if (stateSelectDropdown.isMousePressed)
            return

        // Do not pass click events through if a text field is pressed.
        for (edge in edgeSet) {
            val textField = edge.textField!!
            if (!isSolving && textField.isMousePressed) {
                val graphEdge = graph.getEdge(edge.first.id, edge.second.id)!!
                if (graphEdge.weight == 0.0)
                    edge.textField!!.clear()
                clickedTextfieldEdge = edge
            }
            else if (textField.isFocus) {
                if (isSolving)
                    // Do not focus i.e. edit any textfield when solving.
                    textField.isFocus = false
                else
                    // Submit the text field when the user clicks outside
                    // of it and doesn't press enter like it would be expected.
                    textField.submit()
            }
        }

        if (clickedTextfieldEdge != null)
            return

        // Same goes with any button.
        cp5.getAll(Button::class.java).firstOrNull {
            it.isPressed
        } ?.let {
            clickedButton = it
            return
        }

        val x = mouseX.toFloat()
        val y = mouseY.toFloat()

        val node = createNode(x, y)

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

        // Only create a node when: not solving, it's placeable and the
        // left mouse button was clicked.
        if (!isSolving && isNodePlaceable && mouseButton == LEFT)
            createdNode = node
    }

    private fun edgeLineBetween(node1: Node, node2: Node)
        : Pair<PVector, PVector>
    {
        val a = PVector(node1.x, node1.y)
        val b = PVector(node2.x, node2.y)
        val aToB = PVector.sub(b, a)

        aToB.setMag(node1.radius + node1.strokeWeight)
        val start = PVector(node1.x + aToB.x, node1.y + aToB.y)

        aToB.setMag(node2.radius + node1.strokeWeight)
        val end = PVector(node2.x - aToB.x, node2.y - aToB.y)

        return start to end
    }

    override fun mouseDragged()
    {
        super.mouseDragged()

        hasMouseDragged = true

        clickedTextfieldEdge?.let { edge ->
            if (!isDraggingTextfield) {
                isDraggingTextfield = true
                edge.textField!!.submit()
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
            if (clickedButton != null || mouseButton != LEFT)
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
            if (clickedButton != null || mouseButton != LEFT)
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

    fun boxContains(bx: Float, by: Float,
                    width: Float, height: Float,
                    mX: Float, mY: Float) : Boolean
            = mX > bx && mX < bx + width && mY > by && mY < by + height

    override fun mouseReleased()
    {
        super.mouseReleased()

        clickedButton?.takeIf {
            // ControlP5 does not click a button when the mouse has moved
            // even just a little bit after pressing the mouse button.
            // Circumvent that by checking if the mouse moved and if its
            // button was released inside the button (box).
            hasMouseDragged && boxContains(it.position[0], it.position[1],
                it.width.toFloat(), it.height.toFloat(),
                mouseX.toFloat(), mouseY.toFloat())
        } ?.let { button ->
            // Cannot access the on click handler of the button, nor
            // click it programmatically in any way. Thus
            buttonToListener[button]?.invoke()
            clickedButton = null
            return
        }

        clickedNode?.takeIf {
            // We're not selecting a node if it was dragged around.
            // Also, only select nodes with the right mouse button.
            !isDragging && clickedButton == null
        } ?.let { clicked ->
            // Do not delete nodes nor connect or select nodes when solving.
            if (isSolving) {
                if (isSolvingWithPrim && solutionSteps == null) {
                    primRootNode = clicked
                    val solver = PrimStepwiseMST(graph, clicked.id)
                    solutionSteps = solver.steps().toList()
                    isMstComplete = solver.complete
                    nextStepButton.show()
                    previousStepButton.show()
                    informationTextLabel.setText("Startknoten wurde ausgewählt.")
                }
                return@let
            }

            if (mouseButton == RIGHT) {
                // Remove all references to the node.
                nodeList.remove(clicked)
                nodeMap.remove(clicked.id)
                if (highlightedNode == clicked) highlightedNode = null
                if (selectedNode == clicked) selectedNode = null

                graph.removeVertex(clicked.id)

                // Remove all edges containing this node.
                val iterator = edgeSet.iterator()
                for (edge in iterator)
                    if (edge.hasNode(clicked)) {
                        edgeTextFieldMap.remove(edge.textField!!.id)
                        iterator.remove()
                        cp5.remove(edge.textField!!.name)
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

                        cp5.remove(actualEdge.textField!!.name)
                        edgeSet.remove(actualEdge)
                        edgeMap.remove(edge.first.id to edge.second.id)
                        edgeMap.remove(edge.second.id to edge.first.id)
                        graph.removeEdge(actualEdge.first.id to actualEdge.second.id)
                    }
                    false -> {
                        edge.textField = createTextField()
                        edgeSet.add(edge)
                        edgeMap[edge.first.id to edge.second.id] = edge
                        edgeMap[edge.second.id to edge.first.id] = edge
                        edge.textField!!.isFocus = true
                        graph.addUndirectedEdge(edge.first.id, edge.second.id)
                        edgeTextFieldMap[edge.textField!!.id] = edge
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
        clickedButton = null

        isDragging = false
        isScrolling = false
        isDraggingTextfield = false
        hasMouseDragged = false

        clickedOffsetX = 0f
        clickedOffsetY = 0f
    }

    override fun mouseMoved()
    {
        if (cp5.all.any { it != checkBoxIsChangeable && it.isMouseOver }) {
            highlightedNode = null
            return
        }

        val x = mouseX.toFloat()
        val y = mouseY.toFloat()

        highlightedNode = nodeList.firstOrNull { it.contains(x, y) }
    }

    private fun scrollBy(dx: Float, dy: Float)
    {
        nodeList.forEach { node ->
            node.x += dx
            node.y += dy
        }
    }

    private fun scrollBy(dx: Int, dy: Int)
            = scrollBy(dx.toFloat(), dy.toFloat())

    /* === external stuff === */

    // https://www.openprocessing.org/sketch/107041/
    private fun project(p: PVector, a: PVector, b: PVector, asSegment: Boolean)
            : PVector
    {
        val t = p.x - a.x
        val u = p.y - a.y
        val v = b.x - a.x
        val w = b.y - a.y

        val dot = t * v + u * w
        val len = v * v + w * w
        val ratio = dot / len

        if (asSegment) {
            if (ratio < 0) return a
            if (ratio > 1) return b
        }

        return PVector(a.x + ratio * v, a.y + ratio * w)
    }
}
