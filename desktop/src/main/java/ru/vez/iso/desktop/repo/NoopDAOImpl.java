package ru.vez.iso.desktop.repo;

import ru.vez.iso.shared.dao.ExCardDAO;
import ru.vez.iso.shared.model.ExCard;

import java.util.List;
import java.util.Map;

public class NoopDAOImpl implements ExCardDAO {

    private Map<Long, ExCard> store;

    public NoopDAOImpl(Map<Long, ExCard> store) {
        this.store = store;
    }

    @Override
    public List<ExCard> findAll() {
        return null;
    }

    @Override
    public ExCard findById(Long id) {
        return store.get(id);
    }

    @Override
    public ExCard upsert(ExCard exCard) {
        return store.put(exCard.getId(), exCard);
    }

    @Override
    public boolean delete(ExCard exCard) {
        return store.remove(exCard.getId()) != null;
    }

}
