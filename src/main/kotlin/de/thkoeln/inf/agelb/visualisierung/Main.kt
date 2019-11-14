package de.thkoeln.inf.agelb.visualisierung

import de.thkoeln.inf.agelb.visualisierung.ui.GraphApplet
import processing.core.PApplet

fun main()
{
    val pa = PApplet()

    GraphApplet.run(
        GraphApplet.Config(
            width = 1920 / 2,
            height = 1080 * 3 / 4,
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
}