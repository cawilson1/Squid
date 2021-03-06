/*
 * AbstractRawDataView.java
 *
 * Created Jul 6, 2011
 *
 * Copyright 2006-2017 James F. Bowring and www.Earth-Time.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cirdles.squid.gui.dataViews;

import java.math.BigDecimal;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javax.swing.JLayeredPane;

/**
 *
 * @author James F. Bowring
 */
public abstract class AbstractDataView extends Canvas {

    protected double x;
    protected double y;
    protected double width;
    protected double height;

    /**
     *
     */
    protected double[] myOnPeakData;
    /**
     *
     */
    protected double[] myOnPeakNormalizedAquireTimes;
    /**
     *
     */
    protected int graphWidth;
    /**
     *
     */
    protected int graphHeight;
    /**
     *
     */
    private int topMargin = 0;
    /**
     *
     */
    private int leftMargin = 0;
    /**
     *
     */
    protected double minX;
    /**
     *
     */
    protected double maxX;
    /**
     *
     */
    protected double minY;
    /**
     *
     */
    protected double maxY;
    /**
     *
     */
    private double displayOffsetY = 0;
    /**
     *
     */
    private double displayOffsetX = 0;
    /**
     *
     */
    protected BigDecimal[] tics;

    /**
     *
     */
    public AbstractDataView() {
        super();
    }

    /**
     *
     * @param bounds
     */
    protected AbstractDataView(Rectangle bounds, int leftMargin, int topMargin) {
        super(bounds.getWidth(), bounds.getHeight());
        x = bounds.getX();
        y = bounds.getY();
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;

        this.myOnPeakData = null;

        width = bounds.getWidth();
        height = bounds.getHeight();
        graphWidth = (int) width - leftMargin;
        graphHeight = (int) height - topMargin;

        this.tics = null;
    }

    /**
     *
     * @param g2d
     */
    protected void paintInit(GraphicsContext g2d) {
        relocate(x, y);
        g2d.clearRect(0, 0, width, height);
    }

    /**
     *
     * @param g2d
     */
    public void paint(GraphicsContext g2d) {
        paintInit(g2d);

        drawBorder(g2d);
    }

    private void drawBorder(GraphicsContext g2d) {
        // fill it in
        g2d.setFill(Paint.valueOf("WHITE"));
        g2d.fillRect(0, 0, width, height);

        // draw bordere
        g2d.setStroke(Paint.valueOf("BLACK"));
        g2d.setLineWidth(1);
        g2d.strokeRect(0, 0, width, height);

    }

    /**
     *
     * @param x
     * @return
     */
    public double mapX(double x) {
        return (((x - getMinX_Display()) / getRangeX_Display()) * graphWidth) + leftMargin;
    }

    /**
     *
     * @param y
     * @return
     */
    protected double mapY(double y) {
        return (((getMaxY_Display() - y) / getRangeY_Display()) * graphHeight) + topMargin;
    }

    /**
     *
     * @param doReScale the value of doReScale
     * @param inLiveMode the value of inLiveMode
     */
    public void refreshPanel(boolean doReScale, boolean inLiveMode) {
        try {
            preparePanel();
        } catch (Exception e) {
        }
    }

    /**
     *
     * @param doReScale the value of doReScale
     * @param inLiveMode the value of inLiveMode
     */
    public abstract void preparePanel();

    /**
     * @return the displayOffsetY
     */
    public double getDisplayOffsetY() {
        return displayOffsetY;
    }

    /**
     * @param displayOffsetY the displayOffsetY to set
     */
    public void setDisplayOffsetY(double displayOffsetY) {
        this.displayOffsetY = displayOffsetY;
    }

    /**
     * @return the displayOffsetX
     */
    public double getDisplayOffsetX() {
        return displayOffsetX;
    }

    /**
     * @param displayOffsetX the displayOffsetX to set
     */
    public void setDisplayOffsetX(double displayOffsetX) {
        this.displayOffsetX = displayOffsetX;
    }

    /**
     *
     * @return
     */
    public double getMinX_Display() {
        return minX + getDisplayOffsetX();
    }

    /**
     *
     * @return
     */
    public double getMaxX_Display() {
        return maxX + getDisplayOffsetX();
    }

    /**
     *
     * @return
     */
    public double getMinY_Display() {
        return minY + getDisplayOffsetY();
    }

    /**
     *
     * @return
     */
    public double getMaxY_Display() {
        return maxY + getDisplayOffsetY();
    }

    /**
     *
     * @return
     */
    public double getRangeX_Display() {
        return (getMaxX_Display() - getMinX_Display());
    }

    /**
     *
     * @return
     */
    public double getRangeY_Display() {
        return (getMaxY_Display() - getMinY_Display());
    }

    /**
     * @return the myOnPeakData
     */
    public double[] getMyOnPeakData() {
        return myOnPeakData.clone();
    }

    /**
     * @return the myOnPeakNormalizedAquireTimes
     */
    public double[] getMyOnPeakNormalizedAquireTimes() {
        return myOnPeakNormalizedAquireTimes.clone();
    }

    /**
     *
     * @param x
     * @return
     */
    protected int convertMouseXToValue(int x) {
        return //
                (int) Math.round(
                        (((double) (x - leftMargin)) / (double) graphWidth) //
                        * getRangeX_Display()//
                        + getMinX_Display());
    }

    /**
     *
     * @param y
     * @return
     */
    protected double convertMouseYToValue(double y) {
        return //
                -1 * (((y - topMargin - 1) * getRangeY_Display() / graphHeight) //
                - getMaxY_Display());
    }
}
