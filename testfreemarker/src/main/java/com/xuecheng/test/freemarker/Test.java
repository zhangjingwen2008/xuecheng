package com.xuecheng.test.freemarker;

import javax.xml.soap.Node;
import java.util.*;

public class Test {
    public static void main(String[] args) {
        String s="3[a]2[bc]";
        Stack<String> stack=new Stack<>();
        for(int i=0;i<s.length();i++){
            String ss=String.valueOf(s.charAt(i));
            if(ss.equals("]")){
                String p=stack.pop();

                String letter="";
                while(!p.equals("[")){
                    letter=p+letter;
                    p=stack.pop();
                }
                int k=Integer.parseInt(stack.pop());
                String abc="";
                for(int j=0;j<k;j++){
                    abc+=letter;
                }
//                stack.push(abc);
                while(!(stack.peek()).equals("[")){
                    abc=stack.pop()+abc;
                    stack.push(abc);
                }
            }else{
                stack.push(ss);
            }
        }
        System.out.println(stack.pop());
    }

}
