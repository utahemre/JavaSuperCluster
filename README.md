# JavaSuperCluster
JavaSuperCluster is Geospatial Point Clustering For Java

This repository inspired from <a href="https://github.com/mapbox/supercluster" target="_blank">mapbox/supercluster</a>


#Usage

Create SuperCluster class instance with parameters.

```java
SuperCluster superCluster = new SuperCluster(60, 256, 0, 20, 64, pointArray);
```

#parameters

- radius 
- extent 
- minZoom 
- maxZoom 
- nodeSize 
- featureList 

Get your clustered points with bbox and zoom.

```java
List<Feature> clusters = superCluster.getClusters(new double[]{32.34375, 39.90973623453719, 32.6953125, 40.17887331434696}, 10);
```

<a href="https://utahemre.github.io/geojsonautocompletedemo.html" target="_blank">You can find example here</a>
