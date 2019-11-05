package com.project.test;

import com.project.dao.CommentMapper;
import com.project.pojo.Comment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

class  Location {
    private int id; //id
    private String name; //名称
    private int parentId; //父id

    public Location(int id, String name, int parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    //为了组合多维集合添加的属性
    private List<Location> list;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<Location> getList() {
        return list;
    }

    public void setList(List<Location> list) {
        this.list = list;
    }
}
public class T2 {


    public static void main(String[] args) {

        //一维
        List<Location> list = getChilds(data, 1);
        for(Location l : list){
            System.out.println(l.getName());
        }
        System.out.println("===============");
        //多维
        List<Location> list2 = getChildsManyGroup(data, 1);

    }
    //查询数据库表中所有数据,(这里模拟一个表的所有数据)
    static List<Location> data = new ArrayList<Location>();
    static{
        //new Location(编号, "名称", 父编号);
        Location l = new Location(1, "编程语言", 0);
        Location l1 = new Location(2, "数据库", 0);
        Location l2 = new Location(3, "Java", 1);
        Location l3 = new Location(4, ".NET", 1);
        Location l4 = new Location(5, "java EE", 3);
        Location l5 = new Location(6, "java SE", 3);
        Location l6 = new Location(7, "java ME", 3);
        Location l7 = new Location(8, "asp.NET", 4);
        Location l8 = new Location(9, "ado.NET", 4);
        Location l9 = new Location(10, "MySQL", 2);
        Location l10 = new Location(11, "Oracle", 2);
        Location l11 = new Location(12, "hibernate", 5);
        Location l12 = new Location(13, "hibernate 3.5", 12);
        Location l13 = new Location(14, "hibernate 4.2", 12);
        data.add(l);data.add(l1);data.add(l2);
        data.add(l3);data.add(l9);data.add(l10);
        data.add(l4);data.add(l5);data.add(l6);data.add(l7);data.add(l8);data.add(l11);
        data.add(l12);data.add(l13);
    }

    /**
     * 根据id获取所有子集分类(一维List集合)
     * 1 11 111
     * @param list
     * @param pid
     * @return
     */
    public static List<Location> getChilds(List<Location> list,int pid){
        List<Location> arr = new ArrayList<>();
        for(Location location : list){
            if(pid == location.getParentId()){
                arr.addAll(getChilds(list, location.getId()));
                arr.add(location);
            }
        }
        return arr;
    }
    /**
     * 根据id返回所有子集分类,(多维List集合,List中含有子集List)
     *
     *  1
     *    11
     *       111
     *  2
     *    22
     *       222
     * @param list
     * @param pid
     * @return
     */
    public static List<Location> getChildsManyGroup(List<Location> list,int pid){
        List<Location> arr = new ArrayList<>();
        for(Location location : list){
            if(pid == location.getParentId()){
                location.setList(getChildsManyGroup(list, location.getId()));
                arr.add(location);
            }
        }
        return arr;
    }



}
