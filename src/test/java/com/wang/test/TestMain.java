package com.wang.test;

import java.util.Stack;

/**
 * Created by wangyuhan on 2019/5/7.
 */
public class TestMain<T> {

  private T t;

  public static void main(String[] args) throws NoSuchFieldException {
    ListNode head = new ListNode(1);
    ListNode index = head;
    for(int i = 2; i < 6; i++) {
      ListNode listNode = new ListNode(i);
      head.next = listNode;
      head = head.next;
    }
    Solution solution = new Solution();
    ListNode listNode = solution.reverseKGroup(index, 1);
    System.out.println(listNode);
  }


  static class ListNode {
      int val;
      ListNode next;
      ListNode(int x) { val = x; }
  }


  static class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
      ListNode listNode = new ListNode(1);
      ListNode result = listNode;
      reverse(listNode, head, k);
      return result.next;
    }

    private void reverse(ListNode listNode, ListNode head, int k) {
      ListNode currentHead = head;
      ListNode next;
      Stack<ListNode> stack = new Stack<>();
      int i = 0;
      for(; i < k && head != null; i++, head = head.next) {
        stack.push(head);
      }
      if (i < k) {
        listNode.next = currentHead;
      } else {
        next = stack.peek().next;
        while (!stack.isEmpty()) {
          listNode.next = stack.pop();
          listNode = listNode.next;
        }
        reverse(listNode, next ,k);
      }
    }
  }
}
