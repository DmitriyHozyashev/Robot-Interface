package com.leartgjoni.app.pathfindingsimulator.Graph;

import java.util.*;
public class  Vertex implements Comparable<Vertex>{
    public int id;
    public Vertex parent=null;
    public double d_value=Double.POSITIVE_INFINITY;
    public LinkedList<Edge> edges=new LinkedList<Edge>();
    public boolean discovered = false;
    public double x,y;
    public double h_value=0;
    public double f_value=Double.POSITIVE_INFINITY;

    public Vertex(int id){
        this.id=id;
    }
    public Vertex(int id,double x,double y){
        this.id=id;
        this.x=x;
        this.y=y;
    }

    @Override
    public int compareTo(Vertex o) {
        return Double.compare(this.d_value,o.d_value);
    }




}

