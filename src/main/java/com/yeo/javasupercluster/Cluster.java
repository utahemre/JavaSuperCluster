/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yeo.javasupercluster;

import java.util.Properties;

/**
 *
 * @author yeozkaya@gmail.com
 */
public class Cluster extends MainCluster{
    
    private Properties properties;

    public Cluster(double x, double y, int id, int numPoints) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.numPoints = numPoints;
        this.zoom = 24; //Max Value
        this.parentId = -1;
    }
   
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    
    
    
}
