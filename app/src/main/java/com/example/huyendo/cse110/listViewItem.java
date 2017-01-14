package com.example.sondo.cse110;

/**
 * Created by Son Do on 12/2/2015.
 */
public class listViewItem  {
    String ItemName;
    String ImageName;
    String UserCreated;
    String date;
    public listViewItem(String ImageName, String ItemName, String UserCreated,String date){
        this.ImageName = ImageName;
        this.ItemName = ItemName;
        this.UserCreated = UserCreated;
        this.date = date;
    }
}
