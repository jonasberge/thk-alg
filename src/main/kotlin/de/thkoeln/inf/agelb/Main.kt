package de.thkoeln.inf.agelb

import controlP5.ControlP5
import de.thkoeln.inf.agelb.ui.GraphApplet
import processing.core.PApplet
import java.io.*
import kotlin.math.abs

private var counter = 1
class Foo(val x: Float, val y: Float, radius: Float)

{
    var radius: Float = radius
        set(value) { field = abs(value) }

    val diameter: Float
        get() = radius * 2

    @Transient
    val somethingReadOnly: Int = counter++
}

class FooContainer(val wrapped: Foo)
    : Serializable
{

}

private fun dump(o: Foo)
{
    println("x: ${o.x}, y: ${o.y}, radius: ${o.radius}, " +
            "diameter: ${o.diameter}, somethingReadOnly: ${o.somethingReadOnly}")
}

private fun dump(container: FooContainer)
{
    dump(container.wrapped)
}

class SerializeWrapper(val foo: Foo, val fooContainer: FooContainer)
    : Serializable

fun writeAndRead(fileName: String, serializeWrapper: SerializeWrapper)
    : SerializeWrapper
{
    val file = File(fileName)
    if (!file.exists())
        file.createNewFile()

    val fos = FileOutputStream(file)
    val oos = ObjectOutputStream(fos)
    oos.writeObject(serializeWrapper)
    oos.close()

    // read object from file
    val fis = FileInputStream(fileName)
    val ois = ObjectInputStream(fis)
    val result = ois.readObject() as SerializeWrapper
    ois.close()

    return result
}

fun main()
{
    /*
    val file = File("graphs/cp5.ser")
    if (!file.exists())
        file.createNewFile()

    val pApplet = PApplet()
    val cp5 = ControlP5(pApplet)

    val fos = FileOutputStream(file)
    val oos = ObjectOutputStream(fos)
    oos.writeObject(cp5)
    oos.close()
    */

    /*
    try {
        val first = Foo(1f, 2f, 3f)
        first.somethingReadOnly
        val second = Foo(12f, 24f, 25f)
        val container = FooContainer(second)

        dump(second)
        dump(container)
        println()

        val wrapper = SerializeWrapper(second, container)
        val newWrapper = writeAndRead("graphs/wrapper.ser", wrapper)

        println("equal? ${newWrapper.foo == newWrapper.fooContainer.wrapped}")

        dump(newWrapper.foo)
        dump(newWrapper.fooContainer)

    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }*/

    val pa = PApplet()

    GraphApplet.run(
        GraphApplet.Config(
            width = 1920 / 2,
            height = 1080 / 2,
            isResizable = true,
            backgroundColor = pa.color(240),
            scrollDragTolerance = 8f,
            node = GraphApplet.Config.Node(
                radius = 25.0f,
                padding = 35.0f,
                paddingColor = pa.color(0, 3),
                fillColor = 200,
                selectedFillColor = pa.color(240, 150, 100),
                highlightedFillColor = pa.color(220, 240, 230),
                strokeColor = pa.color(0),
                strokeWeight = 2.0f
            )
        )
    )


    /*
    val graph = Graph()

    graph.connect(1, 2, 6.7)

    graph.addEdge(1, 2, 5.5)

    graph.removeVertex(2)

    graph.removeEdge(1, 2)

    graph.connect(1, 3, 5.2)
    graph.connect(1, 4, 2.8)
    graph.connect(1, 5, 5.6)
    graph.connect(1, 6, 3.6)
    graph.connect(2, 3, 5.7)
    graph.connect(2, 4, 7.3)
    graph.connect(2, 5, 5.1)
    graph.connect(2, 6, 3.2)
    graph.connect(3, 4, 3.4)
    graph.connect(3, 5, 8.5)
    graph.connect(3, 6, 4.0)
    graph.connect(4, 5, 8.0)
    graph.connect(4, 6, 4.4)
    graph.connect(5, 6, 4.6)

    val prim = MSTPrim(graph)
    val result = prim.mst()

    val x = 1
    */

    /*
    val graph = Graph(2, 2)

    graph.addUndirectedEdge(0, 4, 15.0)
    graph.addUndirectedEdge(4, 1, 14.0)
    graph.addUndirectedEdge(1, 5, 11.0)
    graph.addUndirectedEdge(5, 2, 7.0)
    graph.addUndirectedEdge(2, 6, 8.0)
    graph.addUndirectedEdge(6, 3, 17.0)
    graph.addUndirectedEdge(3, 7, 16.0)
    graph.addUndirectedEdge(7, 0, 18.0)
    graph.addUndirectedEdge(0, 1, 5.0)
    graph.addUndirectedEdge(1, 2, 9.0)
    graph.addUndirectedEdge(2, 3, 12.0)
    graph.addUndirectedEdge(3, 0, 6.0)
    graph.addUndirectedEdge(4, 5, 3.0)
    graph.addUndirectedEdge(5, 6, 2.0)
    graph.addUndirectedEdge(6, 7, 4.0)
    graph.addUndirectedEdge(7, 4, 1.0)
    graph.addUndirectedEdge(1, 6, 10.0)
    graph.addUndirectedEdge(1, 7, 13.0)

    println("__Kruskal__")

    KruskalStepwiseMST(graph).steps().forEach {
        println(it)
    }

    println("\n__Prim__")

    PrimStepwiseMST(graph).steps().forEach {
        println(it)
    }
    */
}
