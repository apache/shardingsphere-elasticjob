/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function graph(nodes, links) {
    var svg = d3.select("svg"),  inner = svg.select("g");

    // Create the input graph
    g = new dagreD3.graphlib.Graph({});
    // Set an object for the graph label
    this.g.setGraph({});
    // Default to assigning a new object as a label for each new edge.
    this.g.setDefaultEdgeLabel(function () {
        return {};
    });
    this.g.graph().transition = function (selection) {
        return selection.transition().duration(500);
    };


    // 缩放功能实现
    var zoom = d3.zoom().on("zoom", function() {
        inner.attr("transform", d3.event.transform);
    });
    svg.call(zoom);

    g.nodes().forEach(function (v) {
        var node = g.node(v);
        console.log("Node " + v + ": Label:" + node);
    })

    nodes.forEach(function (node) {
        console.log(node)
        console.log("===")
        g.setNode(node.name, { label: node.desc, class: "node-class", rx:5, ry:5})
    });
    g.removeNode("undefined")
    links.forEach(function (link) {
        g.setEdge(link.source, link.target)
    })

    g.nodes().forEach(function (v) {
        var node = g.node(v);
        console.log("Node " + v + ": Label:" + node);
    })

    // Run the renderer. This is what draws the final graph.
    // Create the renderer
    var render = new dagreD3.render();
    render(inner, g);


    // Center the graph
    var initialScale = 1.25;
    svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 20).scale(initialScale));

    svg.attr('height', g.graph().height * initialScale + 200);

    var selections = inner.selectAll('g.node')
    selections.on('click', function (d) {
        $('#tJobCode').val(d)
        clearEdgeForm()
        activeNodeForm()
    })
    var selectionEdgs = inner.selectAll('g.edgePath')
    selectionEdgs.on('click', function (d) {
        $('#tFromCode').val(d.v)
        $('#tToCode').val(d.w)
        clearNodeForm()
        activeEdgeForm()
    })
    //give IDs to each of the nodes so that they can be accessed
    svg.selectAll("g.node rect")
        .attr("id", function (d) {
            return "node" + d;
        });
    svg.selectAll("g.edgePath path")
        .attr("id", function (e) {
            return e.v + "-" + e.w;
        });
    svg.selectAll("g.edgeLabel g")
        .attr("id", function (e) {
            return 'label_' + e.v + "-" + e.w;
        });
}

function clearGraph() {
    var svg = d3.select("svg"),  inner = svg.select("g");
    g = new dagreD3.graphlib.Graph({});
    this.g.setGraph({});
    this.g.setDefaultEdgeLabel(function () {
        return {};
    });
    this.g.graph().transition = function (selection) {
        return selection.transition().duration(100);
    };
    var render = new dagreD3.render();
    render(inner, g);
}