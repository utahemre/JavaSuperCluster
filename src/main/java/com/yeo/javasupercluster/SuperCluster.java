/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yeo.javasupercluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Point;

/**
 *
 * @author yeozkaya@gmail.com
 */
public class SuperCluster {

    private int minZoom = 0;
    private int maxZoom = 17;
    private int radius = 60;
    private int extent = 512;
    private int nodeSize = 64;
    private final Feature[] points;
    private final KDBush[] trees;

    public SuperCluster(int radius, int extent, int minzoom, int maxzoom, int nodesize,  Feature[] clusterpoints) {

        this.radius = radius;
        this.extent = extent;
        this.minZoom = minzoom;
        this.maxZoom = maxzoom;
        this.nodeSize = nodesize;

        trees = new KDBush[maxZoom + 2];
        this.points = clusterpoints;

        List<MainCluster> clusters = new ArrayList<>();

        for (int i = 0; i < points.length; i++) {
            clusters.add(createPointCluster(points[i], i));
        }

        trees[maxZoom + 1] = new KDBush(clusters, nodeSize);

        for (int z = maxZoom; z >= minZoom; z--) {
            clusters = this._cluster(clusters, z);
            this.trees[z] = new KDBush(clusters, nodeSize);
        }

    }

    private List<MainCluster> _cluster(List<MainCluster> points, int zoom) {
        List<MainCluster> clusters = new ArrayList<>();
        
        double r = radius / (extent * Math.pow(2, zoom));

        for (int i = 0; i < points.size(); i++) {
            MainCluster p = points.get(i);
            
            if (p.getZoom() <= zoom) {
                continue;
            }
            p.setZoom(zoom);

            KDBush tree = this.trees[zoom + 1];
            int[] neighborIds = within(p.getX(), p.getY(), r, tree);

            Integer tempNumPoints = p.getNumPoints();
            int numPoints = tempNumPoints != null ? tempNumPoints : 1;
            double wx = p.getX() * numPoints;
            double wy = p.getY() * numPoints;

            int id = (i << 5) + (zoom + 1);

            int[] list = neighborIds;
            for (int i$1 = 0; i$1 < list.length; i$1 += 1) {
                int neighborId = list[i$1];

                MainCluster b = tree.getPoints().get(neighborId);
                
                if (b.getZoom() <= zoom) {
                    continue;
                }
                b.setZoom(zoom);

                Integer tempNumPoints2 = b.getNumPoints();
                int numPoints2 = tempNumPoints2 != null  ? tempNumPoints2 : 1;
                wx += b.getX() * numPoints2;
                wy += b.getY() * numPoints2;

                numPoints += numPoints2;
                b.setParentId(id);
            }

            if (numPoints == 1) {
                clusters.add(p);
            } else {
                p.setParentId(id);
                clusters.add(createCluster(wx / numPoints, wy / numPoints, id, numPoints));
            }
        }

        return clusters;
    }

    private int[] within(double x, double y, double r, KDBush tree) {
        return within(tree.getIds(), tree.getCoords(), x, y, r, this.nodeSize);
    }

    private int[] within(int[] ids, double[] coords, double qx, double qy, double r, int nodeSize) {
        Stack stack = new Stack();
        stack.push(0);
        stack.push(ids.length - 1);
        stack.push(0);

        List<Integer> result = new ArrayList<>();
        double r2 = r * r;

        while (stack.size() > 0) {
            int axis = (int) stack.pop();
            int right = (int) stack.pop();
            int left = (int) stack.pop();

            if (right - left <= nodeSize) {
                for (int i = left; i <= right; i++) {
                    if (sqDist(coords[2 * i], coords[2 * i + 1], qx, qy) <= r2) {
                        result.add(ids[i]);
                    }
                }
                continue;
            }

            int m = (int) Math.floor((left + right) / 2);

            double x = coords[2 * m];
            double y = coords[2 * m + 1];

            if (sqDist(x, y, qx, qy) <= r2) {
                result.add(ids[m]);
            }

            int nextAxis = (axis + 1) % 2;

            if (axis == 0 ? qx - r <= x : qy - r <= y) {
                stack.push(left);
                stack.push(m - 1);
                stack.push(nextAxis);
            }
            if (axis == 0 ? qx + r >= x : qy + r >= y) {
                stack.push(m + 1);
                stack.push(right);
                stack.push(nextAxis);
            }
        }

        int[] array = result.stream().mapToInt(i -> i).toArray();
        return array;
    }

    private PointCluster createPointCluster(Feature feature, int id) {
        Point p = (Point) feature.getGeometry();

        double x = lngX(p.getCoordinates()[0]);
        double y = latY(p.getCoordinates()[1]);

        return new PointCluster(x, y, 24, id, -1);
    }

    private Cluster createCluster(double x, double y, int id, int numPoints) {
        return new Cluster(x, y, id, numPoints);
    }

    private double lngX(double lng) {
        return lng / 360 + 0.5;
    }

    private double latY(double lat) {
        double sin = Math.sin(lat * Math.PI / 180);
        double y = (0.5 - 0.25 * Math.log((1 + sin) / (1 - sin)) / Math.PI);
        return y < 0 ? 0 : y > 1 ? 1 : y;
    }

    private double xLng(double x) {
        return (x - 0.5) * 360;
    }

    private double yLat(double y) {
        double y2 = (180 - y * 360) * Math.PI / 180;
        return 360 * Math.atan(Math.exp(y2)) / Math.PI - 90;
    }

    private double sqDist(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return dx * dx + dy * dy;
    }

    public List<Feature> getClusters(double[] bbox, int zoom) {
        double minLng = ((bbox[0] + 180) % 360 + 360) % 360 - 180;
        double minLat = Math.max(-90, Math.min(90, bbox[1]));
        double maxLng = bbox[2] == 180 ? 180 : ((bbox[2] + 180) % 360 + 360) % 360 - 180;
        double maxLat = Math.max(-90, Math.min(90, bbox[3]));

        if (bbox[2] - bbox[0] >= 360) {
            minLng = -180;
            maxLng = 180;
        } else if (minLng > maxLng) {
            List<Feature> easternHem = this.getClusters(new double[]{minLng, minLat, 180, maxLat}, zoom);
            List<Feature> westernHem = this.getClusters(new double[]{-180, minLat, maxLng, maxLat}, zoom);
            easternHem.addAll(westernHem);
            return easternHem;
        }

        KDBush tree = this.trees[_limitZoom(zoom)];
        int[] ids = range$1(lngX(minLng), latY(maxLat), lngX(maxLng), latY(minLat), tree);

        List<Feature> clusters = new ArrayList<>();

        for (int i = 0; i < ids.length; i += 1) {
            int id = ids[i];

            MainCluster c = tree.getPoints().get(id);

            clusters.add((c.getNumPoints() != null) ? getClusterJSON(c) : this.points[c.getIndex()]);
        }
        return clusters;
    }

    private int _limitZoom(int z) {
        return Math.max(this.minZoom, Math.min(z, this.maxZoom + 1));
    }

    private int[] range$1(double minX, double minY, double maxX, double maxY, KDBush tree) {
        return range(tree.getIds(), tree.getCoords(), minX, minY, maxX, maxY, this.nodeSize);
    }

    private int[] range(int[] ids, double[] coords, double minX, double minY, double maxX, double maxY, int nodeSize) {

        Stack stack = new Stack();
        stack.push(0);
        stack.push(ids.length - 1);
        stack.push(0);

        List<Integer> result = new ArrayList<>();
        double x, y;

        while (stack.size() > 0) {
            int axis = (int) stack.pop();
            int right = (int) stack.pop();
            int left = (int) stack.pop();

            if (right - left <= nodeSize) {
                for (int i = left; i <= right; i++) {
                    x = coords[2 * i];
                    y = coords[2 * i + 1];
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                        result.add(ids[i]);
                    }
                }
                continue;
            }

            int m = (int) Math.floor((left + right) / 2);

            x = coords[2 * m];
            y = coords[2 * m + 1];

            if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                result.add(ids[m]);
            }

            int nextAxis = (axis + 1) % 2;

            if (axis == 0 ? minX <= x : minY <= y) {
                stack.push(left);
                stack.push(m - 1);
                stack.push(nextAxis);
            }
            if (axis == 0 ? maxX >= x : maxY >= y) {
                stack.push(m + 1);
                stack.push(right);
                stack.push(nextAxis);
            }
        }

        int[] array = result.stream().mapToInt(i -> i).toArray();
        return array;
    }

    private Feature getClusterJSON(MainCluster cluster) {

        Point point = new Point(new double[]{xLng(cluster.getX()), yLat(cluster.getY())});
        Feature aFeature = new Feature(point, getClusterProperties(cluster));

        return aFeature;

    }

    private Map getClusterProperties(MainCluster cluster) {
        int count = cluster.numPoints;
        String abbrev = (count >= 1000000) ? ((Math.round(count / 1000000)) + "M") : (count >= 10000) ? ((Math.round(count / 1000)) + "K") : ((count >= 1000) ? ((Math.round(count / 100) / 10) + "K") : count + "");

        Map<String, Object> properties = new HashMap();

        properties.put("cluster", true);
        properties.put("cluster_id", cluster.getId());
        properties.put("point_count", count);
        properties.put("point_count_abbreviated", abbrev);

        return properties;

    }

}
