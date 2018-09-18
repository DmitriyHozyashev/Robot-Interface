package com.leartgjoni.app.pathfindingsimulator.Graph;

public class Edge {
    public Vertex source;
    public Vertex destination;
    public double weight;
    public int isPath=0;
    public Edge(Vertex source,Vertex destination,double weight){
        this.source=source;
        this.destination=destination;
        this.weight=weight;
    }
}

