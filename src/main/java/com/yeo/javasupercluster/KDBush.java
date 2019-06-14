/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yeo.javasupercluster;

import java.util.List;

/**
 *
 * @author yeozkaya@gmail.com
 */
public class KDBush {

    private double[] coords;
    private int[] ids;
    private int nodeSize;
    private List<MainCluster> points;

    public KDBush(List<MainCluster> points, int nodeSize) {
        this.nodeSize = nodeSize;
        this.points = points;

        this.ids = new int[points.size()];
        this.coords = new double[points.size() * 2];

        for (int i = 0; i < points.size(); i++) {
            ids[i] = i;
            coords[2 * i] = points.get(i).getX();
            coords[2 * i + 1] = points.get(i).getY();
        }
        sortKD(ids, coords, nodeSize, 0, ids.length - 1, 0);
    }

    private void sortKD(int[] ids, double[] coords, int nodeSize, int left, int right, int depth) {
        if (right - left <= nodeSize) {
            return;
        }

        int m = (left + right) >> 1;

        select(ids, coords, m, left, right, depth % 2);

        sortKD(ids, coords, nodeSize, left, m - 1, depth + 1);
        sortKD(ids, coords, nodeSize, m + 1, right, depth + 1);
    }

    private void select(int[] ids, double[] coords, int k, int left, int right, int inc) {

        while (right > left) {
            if (right - left > 600) {
                int n = right - left + 1;
                int m = k - left + 1;
                double z = Math.log(n);
                double s = 0.5 * Math.exp(2 * z / 3);
                double sd = 0.5 * Math.sqrt(z * s * (n - s) / n) * (m - n / 2 < 0 ? -1 : 1);
                int newLeft = (int) Math.max(left, Math.floor(k - m * s / n + sd));
                int newRight = (int) Math.min(right, Math.floor(k + (n - m) * s / n + sd));
                select(ids, coords, k, newLeft, newRight, inc);
            }

            double t = coords[2 * k + inc];
            int i = left;
            int j = right;

            swapItem(ids, coords, left, k);
            if (coords[2 * right + inc] > t) {
                swapItem(ids, coords, left, right);
            }

            while (i < j) {
                swapItem(ids, coords, i, j);
                i++;
                j--;
                while (coords[2 * i + inc] < t) {
                    i++;
                }
                while (coords[2 * j + inc] > t) {
                    j--;
                }
            }

            if (coords[2 * left + inc] == t) {
                swapItem(ids, coords, left, j);
            } else {
                j++;
                swapItem(ids, coords, j, right);
            }

            if (j <= k) {
                left = j + 1;
            }
            if (k <= j) {
                right = j - 1;
            }
        }
    }

    private void swapItem(int[] ids, double[] coords, int i, int j) {
        swap(ids, i, j);
        swap(coords, 2 * i, 2 * j);
        swap(coords, 2 * i + 1, 2 * j + 1);
    }

    private void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private void swap(double[] arr, int i, int j) {
        double tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }

    public int[] getIds() {
        return ids;
    }

    public void setIds(int[] ids) {
        this.ids = ids;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(int nodeSize) {
        this.nodeSize = nodeSize;
    }

    public List<MainCluster> getPoints() {
        return points;
    }

    public void setPoints(List<MainCluster> points) {
        this.points = points;
    }
    
}
