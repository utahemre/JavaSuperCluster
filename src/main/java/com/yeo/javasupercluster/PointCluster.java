/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yeo.javasupercluster;

/**
 *
 * @author yeozkaya@gmail.com
 */
public class PointCluster extends MainCluster{


    public PointCluster(double x, double y, int zoom, int index, int parentId) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.index = index;
        this.parentId = parentId;
    }
}
