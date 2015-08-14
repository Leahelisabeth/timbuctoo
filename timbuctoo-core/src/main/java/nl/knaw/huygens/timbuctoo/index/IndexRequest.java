package nl.knaw.huygens.timbuctoo.index;

public class IndexRequest {

  public static final String INDEX_ALL = "Index all";
  private String desc;

  public static IndexRequest indexAll() {
    IndexRequest indexRequest = new IndexRequest();
    indexRequest.setDesc(INDEX_ALL);

    return indexRequest;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}
