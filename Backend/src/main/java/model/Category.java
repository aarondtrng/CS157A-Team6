package model;

/** Item category (Category table). Plain bean so JSP EL can read it on Tomcat 10.1. */
public class Category {
    private final int categoryId;
    private final String categoryName;

    public Category(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
