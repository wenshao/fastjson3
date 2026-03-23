package com.alibaba.fastjson3.benchmark.eishay.vo;

import java.util.Objects;

public class Image {
    public enum Size {
        SMALL, LARGE
    }

    private int height;
    private Size size;
    private String title;
    private String uri;
    private int width;

    public Image() {
    }

    public Image(String uri, String title, int width, int height, Size size) {
        this.height = height;
        this.title = title;
        this.uri = uri;
        this.width = width;
        this.size = size;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Image image = (Image) o;
        return height == image.height && width == image.width && size == image.size
                && Objects.equals(title, image.title) && Objects.equals(uri, image.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, size, title, uri, width);
    }
}
