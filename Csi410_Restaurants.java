/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csi410_restaurants;

import java.sql.*; 
 
public class Csi410_Restaurants {

   
    public static void main(String[] args) {
       Csi410_Restaurants res = new Csi410_Restaurants(); 
       
       System.out.println(res); 
    }
    
    
    public Connection get_Connection(){
        
        Connection conn = null; 
        
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");//mysql connector driver 
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Restaurant_Reservations", "root", ""); //location of database 
            
        }catch(Exception e){
            System.out.println(e); 
        }
        return conn; 
    }
    
}
