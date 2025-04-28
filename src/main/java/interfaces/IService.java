package interfaces;

import java.util.List;

public interface IService<T> {
    boolean add(T t);
    void update(T t);
    void delete(int id);
    List<T> getAll();

}