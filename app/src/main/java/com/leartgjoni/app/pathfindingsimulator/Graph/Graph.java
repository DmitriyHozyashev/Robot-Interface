package com.leartgjoni.app.pathfindingsimulator.Graph;

import java.util.*;
public class Graph {

    public ArrayList<Vertex> vertex=new ArrayList<Vertex>();

    public void addVertex(int id){
        Vertex v1 = new Vertex(id);
        vertex.add(v1);
    }
    public void addVertex(int id,double x,double y){
        Vertex v1 = new Vertex(id,x,y);
        vertex.add(v1);
    }
    public void addEdge(Vertex source,Vertex destination,double weight){
        source.edges.add(new Edge(source,destination,weight));
        destination.edges.add(new Edge(destination,source,weight));
    }
    public void addEdgeGrid(Vertex source,Vertex destination,double weight){
        source.edges.add(new Edge(source,destination,weight));
    }
    public Vertex getV(double x,double y){
        for(int i=0;i<vertex.size();i++)
            if(vertex.get(i).x==x&&vertex.get(i).y==y)
                return vertex.get(i);
        return null;
    }

    public void linkedlist(){
        for(int i=0;i<vertex.size();i++){
            System.out.print(vertex.get(i).id+": ");
            for(int j=0;j<vertex.get(i).edges.size();j++){
                System.out.print("=>"+vertex.get(i).edges.get(j).destination.id);
            }
            System.out.println();
        }
    }

    public void heuristic(Vertex v,Vertex destination){
        v.h_value=Math.sqrt((v.x-destination.x)*(v.x-destination.x)+
                (v.y-destination.y)*(v.y-destination.y));
    }
}

