package habr.telegram.bot.habrtelegrambot.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Post {
  private String url;
  private String header;
  private LocalDateTime dateTime;

  public Post(String url, String header, LocalDateTime dateTime) {
    this.url = url;
    this.header = header;
    this.dateTime = dateTime;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Post post = (Post) o;
    return Objects.equals(url, post.url) &&
        Objects.equals(header, post.header) &&
        Objects.equals(dateTime, post.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, header, dateTime);
  }

  @Override
  public String toString() {
    return "Post{" +
        "url='" + url + '\'' +
        ", header='" + header + '\'' +
        ", dateTime=" + dateTime +
        '}';
  }
}
