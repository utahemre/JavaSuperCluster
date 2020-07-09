/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

import com.yeo.javasupercluster.SuperCluster;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;

/**
 *
 * @author yeozkaya@gmail.com
 */
public class SuperClusterExample {

    public static void main(String args[]) {

        try {
            InputStream inputStream = SuperClusterExample.class.getClassLoader().getResourceAsStream("turkey.json");
            String geoJson = IOUtils.toString(inputStream, "UTF-8");
            FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(geoJson);
            
            Feature[] pointArray = featureCollection.getFeatures();

            SuperCluster superCluster = new SuperCluster(60, 256, 0, 20, 64, pointArray);

            List<Feature> clusters = superCluster.getClusters(new double[]{32.34375, 39.90973623453719, 32.6953125, 40.17887331434696}, 10);
            
            System.out.println("Cluster count in this bbox is " + clusters.size());
        } catch (IOException ex) {
            Logger.getLogger(SuperClusterExample.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
