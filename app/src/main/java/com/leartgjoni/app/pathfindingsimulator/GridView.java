package com.leartgjoni.app.pathfindingsimulator;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leartgjoni.app.pathfindingsimulator.Graph.Edge;
import com.leartgjoni.app.pathfindingsimulator.Graph.Graph;
import com.leartgjoni.app.pathfindingsimulator.Graph.Vertex;
import com.leartgjoni.app.pathfindingsimulator.Graph.astarcomparator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Created by Delivus on 16-08-18.
 */
public class GridView extends View{
    int rows=20;
    int cols=26;
    float tileWidth;
    float tileHeight;
    float width;
    float height;
    int[][] grid = new int[rows][cols];
    int startClicked = 0;
    int start_x,start_y;
    int stop_x,stop_y;
    int endPoint = 0;
    int stoped = 0;
    int cont = 0;

    Graph graph;
    GridActivity act;
    Paint paint = new Paint();
    Async astarthread;

    ArrayList<ArrayList<Integer>> path;

    public GridView(Context context){
        super(context);
        init(null,0);
    }
    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }
    public GridView(Context context,AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    public void init(AttributeSet attrs,int defStyle){
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
    }

    public void init2(GridActivity activity){
        act = activity;
    }

    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        tileWidth = width/cols;
        tileHeight = height/rows;
        int border=0;
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        for(int r=0;r<rows;r++)
            for(int c=0;c<cols;c++)
            {
                paint.setStyle(Paint.Style.FILL);
                int value = grid[r][c];
                switch(value){
                    case 0:
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.BLACK);
                        break;
                    case 1:
                        paint.setColor(Color.RED);
                        break;
                    case 2:
                        paint.setColor(Color.GREEN);
                        break;
                    case 3:
                        paint.setColor(Color.BLACK);
                        break;
                    case 4:
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.BLACK);
                        border=1;
                        break;
                    case 5:
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.BLACK);
                        border=1;
                        break;
                    case 6:
                        paint.setColor(Color.YELLOW);
                        border=1;
                        break;
                }
                canvas.drawRect(c*tileWidth, r*tileHeight,c*tileWidth+tileWidth ,r*tileHeight+tileHeight , paint);
                if(border==1){
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(c * tileWidth, r * tileHeight, c * tileWidth + tileWidth, r * tileHeight + tileHeight, paint);
                }

            }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = View.MeasureSpec.getSize(widthMeasureSpec);
        height = View.MeasureSpec.getSize(heightMeasureSpec);
    }

    public boolean onTouchEvent(MotionEvent motionEvent){
        if(motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            int c = (int) (x / tileWidth);
            int r = (int) (y / tileHeight);
            if(endPoint==1 && cont==1){
                if(stoped == 0) {
                    if (grid[r][c] != 3) {
                        grid[stop_y][stop_x] = 0;
                        grid[r][c] = 2;
                        stop_x = c;
                        stop_y = r;
                        if (astarthread != null)
                            astarthread.cancel(true);
                        Astar();
                    }
                }
            }
            if(startClicked==1 && cont == 0) {
                if (grid[r][c] != 3) {
                    grid[start_y][start_x] = 0;
                    grid[r][c] = 1;
                    start_x = c;
                    start_y = r;
                    startClicked = 0;
                    cont = 1;
                    endPoint = 1;
                }
            }
        }
        if(motionEvent.getAction()==MotionEvent.ACTION_MOVE && endPoint!=1){
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            int c = (int) (x / tileWidth);
            int r = (int) (y / tileHeight);
            if(x>width || y>height||x<0||y<0)
                return false;
            if(grid[r][c]!=1&&grid[r][c]!=2)
            if (grid[r][c] != 3)
                grid[r][c] = 3;
        }
        invalidate();
        return true;
    }

    public void Astar() {
        getGraph();
        astarthread = new Async();
        astarthread.execute();
        getGraph();
    }

    public void getGraph(){
        Graph g = new Graph();
        int counter=0;
        for(int i=0;i<rows;i++) {
            for (int j = 0; j < cols; j++) {
                if(grid[i][j]!=3)
                g.addVertex(counter, j, i);
                if(grid[i][j]==4||grid[i][j]==5||grid[i][j]==6)
                    grid[i][j]=0;
                counter++;
            }
        }
        int x1,x2,x3,x4,x5,x6,x7,x8;
        int y1,y2,y3,y4,y5,y6,y7,y8;

        for(int i=0;i<rows;i++) {
            for (int j = 0; j < cols; j++){
                if(grid[i][j]==3)
                    continue;
                x1=j-1;y1=i-1;
                x2=j;y2=i-1;
                x3=j+1;y3=i-1;
                x4=j-1;y4=i;
                x5=j+1;y5=i;
                x6=j-1;y6=i+1;
                x7=j;y7=i+1;
                x8=j+1;y8=i+1;
                if(x1>=0&&x1<cols&&y1>=0&&y1<rows)
                    if(grid[y1][x1]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x1,y1),1.4);
                if(x2>=0&&x2<cols&&y2>=0&&y2<rows)
                    if(grid[y2][x2]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x2,y2),1);
                if(x3>=0&&x3<cols&&y3>=0&&y3<rows)
                    if(grid[y3][x3]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x3,y3),1.4);
                if(x4>=0&&x4<cols&&y4>=0&&y4<rows)
                    if(grid[y4][x4]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x4,y4),1);
                if(x5>=0&&x5<cols&&y5>=0&&y5<rows)
                    if(grid[y5][x5]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x5,y5),1);
                if(x6>=0&&x6<cols&&y6>=0&&y6<rows)
                    if(grid[y6][x6]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x6,y6),1.4);
                if(x7>=0&&x7<cols&&y7>=0&&y7<rows)
                    if(grid[y7][x7]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x7,y7),1);
                if(x8>=0&&x8<cols&&y8>=0&&y8<rows)
                    if(grid[y8][x8]!=3)
                    g.addEdgeGrid(g.getV(j,i),g.getV(x8,y8),1.4);
            }
        }
        graph = g;
        invalidate();
    }

    public class Async extends com.leartgjoni.app.pathfindingsimulator.Async {

        @Override
        protected Void doInBackground(Void... params) {

            Vertex start = graph.getV(start_x, start_y);
            Vertex destination = graph.getV(stop_x, stop_y);

            LinkedList<Vertex> CLOSED = new LinkedList<Vertex>();
            astarcomparator comparator = new astarcomparator();
            PriorityQueue<Vertex> OPEN = new PriorityQueue<Vertex>(1, comparator);
            start.d_value = 0;
            start.f_value = 0;
            OPEN.add(start);

            while (!OPEN.isEmpty()) {
                if (isCancelled()) break;
                Vertex extracted = OPEN.poll();
                extracted.discovered = true;
                CLOSED.add(extracted);
                if(grid[(int) extracted.y][(int) extracted.x]!=1&&grid[(int) extracted.y][(int) extracted.x]!=2)
                grid[(int)extracted.y][(int)extracted.x]=4;
                publishProgress();
                if (extracted == destination) {
                    break;
                }

                for (int i = 0; i < extracted.edges.size(); i++) {
                    Edge edge = extracted.edges.get(i);
                    Vertex neighbor = edge.destination;
                    if (neighbor.discovered == false) {
                        if(grid[(int) neighbor.y][(int) neighbor.x]!=1&&grid[(int) neighbor.y][(int) neighbor.x]!=2)
                        grid[(int)neighbor.y][(int)neighbor.x]=5;
                        publishProgress();
                        graph.heuristic(neighbor, destination);
                        if (neighbor.f_value > extracted.f_value + edge.weight) {
                            neighbor.d_value = extracted.d_value + edge.weight;
                            graph.heuristic(neighbor, destination);
                            neighbor.f_value = neighbor.d_value + neighbor.h_value;
                            neighbor.parent = extracted;
                            OPEN.remove(neighbor);
                            OPEN.add(neighbor);
                        }
                    }
                }
            }
            if(destination.parent!=null) {
                Vertex current = destination;
                path = new ArrayList<>();
                while (current != null) {
                    if (isCancelled()) break;
                    if (grid[(int) current.y][(int) current.x] == 2) {
                        ArrayList<Integer> pathLine = new ArrayList<>();
                        pathLine.add((int) current.y);
                        pathLine.add((int) current.x);
                        path.add(0, pathLine);
                    }
                    if (grid[(int) current.y][(int) current.x] != 1 && grid[(int) current.y][(int) current.x] != 2) {
                        grid[(int) current.y][(int) current.x] = 6;
                        ArrayList<Integer> pathLine = new ArrayList<>();
                        pathLine.add((int) current.y);
                        pathLine.add((int) current.x);
                        path.add(0, pathLine);
                    }
                    publishProgress();
                    current = current.parent;
                }
                for (int i = 0; i < path.size(); i++) {
                    if (!isCancelled()) {
                        ArrayList<Integer> pathLine = path.get(i);
                        int sty = pathLine.get(0);
                        int stx = pathLine.get(1);
                        if (sty == start_y+1)
                        {
                            if (stx == start_x-1)
                                act.relocaton("1");
                            if (stx == start_x)
                                act.relocaton("2");
                            if (stx == start_x+1)
                                act.relocaton("3");
                        }
                        if (sty == start_y)
                        {
                            if (stx == start_x+1)
                                act.relocaton("4");
                            if (stx == start_x-1)
                                act.relocaton("8");
                        }
                        if (sty == start_y-1)
                        {
                            if (stx == start_x+1)
                                act.relocaton("5");
                            if (stx == start_x)
                                act.relocaton("6");
                            if (stx == start_x-1)
                                act.relocaton("7");
                        }
                        grid[start_y][start_x] = 0;
                        grid[sty][stx] = 1;
                        start_x = stx;
                        start_y = sty;
                        publishProgress();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else return null;
                }
            }
            return null;
        }

        protected void onProgressUpdate(Void... values) {
            invalidate();
        }


        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
        }

    }
}
