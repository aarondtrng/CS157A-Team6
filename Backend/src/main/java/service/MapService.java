package service;

import dao.ItemDao;
import model.MapItem;

import java.util.List;

/** Loads the items shown on the campus map. */
public class MapService {
    private final ItemDao itemDao;

    public MapService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    /** @throws dao.DataAccessException if the database cannot be read */
    public List<MapItem> getMapItems() {
        return itemDao.findMapItems();
    }

}
