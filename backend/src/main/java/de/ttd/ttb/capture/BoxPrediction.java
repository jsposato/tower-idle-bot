package de.ttd.ttb.capture;

import org.opencv.core.Rect;

public record BoxPrediction(Rect box, Pixel center, Prediction prediction) {}
