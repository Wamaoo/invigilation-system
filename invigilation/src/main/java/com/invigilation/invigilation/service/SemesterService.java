package com.invigilation.invigilation.service;

import com.invigilation.invigilation.entity.Semester;
import java.util.List;

public interface SemesterService {
    List<Semester> getAll();
    Semester getById(Integer id);
    Semester getCurrent();
    boolean add(Semester semester);
    boolean update(Semester semester);
    boolean delete(Integer id);
    boolean setCurrent(Integer id);
}
