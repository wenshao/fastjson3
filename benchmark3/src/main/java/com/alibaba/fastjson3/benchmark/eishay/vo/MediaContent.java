package com.alibaba.fastjson3.benchmark.eishay.vo;

import java.util.List;
import java.util.Objects;

public class MediaContent {
    private Media media;
    private List<Image> images;

    public MediaContent() {
    }

    public MediaContent(Media media, List<Image> images) {
        this.media = media;
        this.images = images;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaContent that = (MediaContent) o;
        return Objects.equals(media, that.media) && Objects.equals(images, that.images);
    }

    @Override
    public int hashCode() {
        return Objects.hash(media, images);
    }
}
