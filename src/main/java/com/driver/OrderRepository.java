package com.driver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Repository
public class OrderRepository {
    private HashMap<String,Order> orderHashMap;
    private HashMap<String,DeliveryPartner> deliveryPartnerHashMap;
    private HashMap<String, String> orderPartnerHashMap;
    private HashMap<String, HashSet<String>> partnerOrderHashMap;

    public OrderRepository() {
        this.orderHashMap = new HashMap<>();
        this.deliveryPartnerHashMap = new HashMap<>();
        this.orderPartnerHashMap = new HashMap<>();
        this.partnerOrderHashMap = new HashMap<>();
    }

    public String addOrder(Order order){
        orderHashMap.put(order.getId(),order);
        return "ok";
    }

    public String addPartner(String partnerId){
        DeliveryPartner d = new DeliveryPartner(partnerId);
        deliveryPartnerHashMap.put(partnerId,d);
        return "ok";
    }

    public String addOrderPartnerPair(String orderId, String partnerId){

//        if(orderPartnerPairHashMap.containsKey(partnerId)){
//            List l1 = orderPartnerPairHashMap.get(partnerId);
//            Order order=orderHashMap.get(orderId);
//            l1.add(order);
//            orderPartnerPairHashMap.put(partnerId,l1);
//        }else {
//            List<Order> orders= new ArrayList<>();
//            Order order1=orderHashMap.get(orderId);
//            orders.add(order1);
//            orderPartnerPairHashMap.put(partnerId,orders);
//        }
//
//        //This is basically assigning that order to that partnerId
//        return "ok";
        if(orderHashMap.containsKey(orderId) && deliveryPartnerHashMap.containsKey(partnerId)){

            HashSet<String> currentOrders = new HashSet<String>();
            if(partnerOrderHashMap.containsKey(partnerId))
                currentOrders = partnerOrderHashMap.get(partnerId);
            currentOrders.add(orderId);
            partnerOrderHashMap.put(partnerId, currentOrders);

            DeliveryPartner partner = deliveryPartnerHashMap.get(partnerId);
            partner.setNumberOfOrders(currentOrders.size());
            orderPartnerHashMap.put(orderId, partnerId);
        }
        return "ok";
    }

    public Order getOrderById(String orderId){
        Order order1=null;
        for (String order:orderHashMap.keySet()){
            if(order.equals(orderId)){
                order1=orderHashMap.get(order);
            }
        }
        return order1;
    }

    public DeliveryPartner getPartnerById(String partnerId){
        DeliveryPartner partner1=null;
        for (String partner:deliveryPartnerHashMap.keySet()){
            if(partner.equals(partnerId)){
                partner1=deliveryPartnerHashMap.get(partner);
            }
        }
        return partner1;
    }

    public int getOrderCountByPartnerId(String partnerId){

        int orderCount = 0;

        //orderCount should denote the orders given by a partner-id

        for (String partner:deliveryPartnerHashMap.keySet()){
            if(partner.equals(partnerId)){
                orderCount=deliveryPartnerHashMap.get(partner).getNumberOfOrders();
            }
        }
        return orderCount;
    }

    public List<String> getOrdersByPartnerId(String partnerId){
//        List<String> orders = null;
//        List<Order> orderList=null;
//
//        //orders should contain a list of orders by PartnerId
//        if(orderPartnerPairHashMap.containsKey(partnerId)){
//            orderList=orderPartnerPairHashMap.get(partnerId);
//        }
//        for (int i=0; i<orderList.size(); i++){
//            String id = orderList.get(i).getId();
//            orders.add(id);
//        }
        HashSet<String> orderList = new HashSet<>();
        if(partnerOrderHashMap.containsKey(partnerId)) orderList = partnerOrderHashMap.get(partnerId);
        return new ArrayList<>(orderList);
    }

    public List<String> getAllOrders(){
        List<String> orders = null;
        for (String ordersId: orderHashMap.keySet()){
            orders.add(ordersId);
        }
        //Get all orders
        return orders;
    }

    public Integer getCountOfUnassignedOrders() {
        Integer countOfOrders = 0;
        List<String> orders =  new ArrayList<>(orderHashMap.keySet());
        for(String orderId: orders){
            if(!orderPartnerHashMap.containsKey(orderId)){
                countOfOrders += 1;
            }
        }
        return countOfOrders;
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String timeS, String partnerId) {
        Integer hour = Integer.valueOf(timeS.substring(0, 2));
        Integer minutes = Integer.valueOf(timeS.substring(3));
        Integer time = hour*60 + minutes;

        Integer countOfOrders = 0;
        if(partnerOrderHashMap.containsKey(partnerId)){
            HashSet<String> orders = partnerOrderHashMap.get(partnerId);
            for(String order: orders){
                if(orderHashMap.containsKey(order)){
                    Order currOrder = orderHashMap.get(order);
                    if(time < currOrder.getDeliveryTime()){
                        countOfOrders += 1;
                    }
                }
            }
        }
        return countOfOrders;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId) {
        Integer time = 0;

        if(partnerOrderHashMap.containsKey(partnerId)){
            HashSet<String> orders = partnerOrderHashMap.get(partnerId);
            for(String order: orders){
                if(orderHashMap.containsKey(order)){
                    Order currOrder = orderHashMap.get(order);
                    time = Math.max(time, currOrder.getDeliveryTime());
                }
            }
        }

        Integer hour = time/60;
        Integer minutes = time%60;

        String hourInString = String.valueOf(hour);
        String minInString = String.valueOf(minutes);
        if(hourInString.length() == 1){
            hourInString = "0" + hourInString;
        }
        if(minInString.length() == 1){
            minInString = "0" + minInString;
        }

        return  hourInString + ":" + minInString;
    }

    public void deletePartner(String partnerId) {
        HashSet<String> orders = new HashSet<>();
        if(partnerOrderHashMap.containsKey(partnerId)){
            orders = partnerOrderHashMap.get(partnerId);
            for(String order: orders){
                if(orderPartnerHashMap.containsKey(order)){

                    orderPartnerHashMap.remove(order);
                }
            }
            partnerOrderHashMap.remove(partnerId);
        }

        if(deliveryPartnerHashMap.containsKey(partnerId)){
            deliveryPartnerHashMap.remove(partnerId);
        }
    }


    public void deleteOrderById(String orderId) {
        if(orderPartnerHashMap.containsKey(orderId)){
            String partnerId = orderPartnerHashMap.get(orderId);
            HashSet<String> orders = partnerOrderHashMap.get(partnerId);
            orders.remove(orderId);
            partnerOrderHashMap.put(partnerId, orders);

            //change order count of partner
            DeliveryPartner partner = deliveryPartnerHashMap.get(partnerId);
            partner.setNumberOfOrders(orders.size());
        }

        if(orderHashMap.containsKey(orderId)){
            orderHashMap.remove(orderId);
        }

    }




}
