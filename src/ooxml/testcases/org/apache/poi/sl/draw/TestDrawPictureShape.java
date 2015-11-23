/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.sl.draw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.RectAlign;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.Test;

public class TestDrawPictureShape {
    final static POIDataSamples ssSamples = POIDataSamples.getSlideShowInstance();
    
    /** a generic way to open a sample slideshow document **/
    public static SlideShow<?,?> openSampleDocument(String sampleName) throws IOException {
        InputStream is = ssSamples.openResourceAsStream(sampleName);
        try {
            return SlideShowFactory.create(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            is.close();
        }
    }

    @Test
    public void testResize() throws Exception {
        String files[] = { "pictures.ppt", "shapes.pptx" };
        for (String file : files) {
            SlideShow<?,?> ss = openSampleDocument(file);
            
            Slide<?,?> slide = ss.getSlides().get(0);
            PictureShape<?,?> picShape = null;
            for (Shape<?,?> shape : slide.getShapes()) {
                if (shape instanceof PictureShape) {
                    picShape = (PictureShape<?,?>)shape;
                    break;
                }
            }
            assertNotNull(picShape);
            PictureData pd = picShape.getPictureData();
            Dimension dimPd = pd.getImageDimension();
            new DrawPictureShape(picShape).resize();
            Dimension dimShape = new Dimension(
                (int)picShape.getAnchor().getWidth(),
                (int)picShape.getAnchor().getHeight()
            );
            assertEquals(dimPd, dimShape);
            
            double newWidth = (dimPd.getWidth()*(100d/dimPd.getHeight()));
            // ... -1 is a rounding error
            Rectangle2D expRect = new Rectangle2D.Double(rbf(50+300-newWidth, picShape), 50, rbf(newWidth, picShape), 100);
            Rectangle2D target = new Rectangle2D.Double(50,50,300,100);
            new DrawPictureShape(picShape).resize(target, RectAlign.BOTTOM_RIGHT);
            Rectangle2D actRect = picShape.getAnchor();
            assertEquals(expRect.getX(), actRect.getX(), .0001);
            assertEquals(expRect.getY(), actRect.getY(), .0001);
            assertEquals(expRect.getWidth(), actRect.getWidth(), .0001);
            assertEquals(expRect.getHeight(), actRect.getHeight(), .0001);
            ss.close();
        }
    }
    
    // round back and forth - points -> master -> points
    static double rbf(double val, PictureShape<?,?> picShape) {
        if (picShape.getClass().getName().contains("HSLF")) {
            return Units.masterToPoints(Units.pointsToMaster(val));
        } else {
            return val;
        }
    }
}
