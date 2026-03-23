package com.alibaba.fastjson3.benchmark.eishay.vo;

import java.util.List;
import java.util.Objects;

public class Media {
    public enum Player {
        JAVA, FLASH
    }

    private int bitrate;
    private long duration;
    private String format;
    private int height;
    private List<String> persons;
    private Player player;
    private long size;
    private String title;
    private String uri;
    private int width;
    private String copyright;

    public Media() {
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<String> getPersons() {
        return persons;
    }

    public void setPersons(List<String> persons) {
        this.persons = persons;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
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

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Media media = (Media) o;
        return bitrate == media.bitrate && duration == media.duration && height == media.height
                && size == media.size && width == media.width
                && Objects.equals(format, media.format) && Objects.equals(persons, media.persons)
                && player == media.player && Objects.equals(title, media.title)
                && Objects.equals(uri, media.uri) && Objects.equals(copyright, media.copyright);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitrate, duration, format, height, persons, player, size, title, uri, width, copyright);
    }
}
