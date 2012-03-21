package org.pentaho.di.trans.steps.mailinput;

import java.util.Iterator;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;


public class BatchFolderIterator implements Iterator<Message>{

  private static final int SIZE_ERR = -1;

  private int start;
  private int end;
  
  private int batchSize;

  private int batchFirst;
  private int batchLast;

  
  private Folder folder;
  
  private Message[] messages;
  private int buffIndex;
  
  private int msgCount;
  
  public BatchFolderIterator(Folder folder, int batchSize){
    this(folder,batchSize,null,null);
  }
  
  public BatchFolderIterator(Folder folder, Integer batchSize, Integer start, Integer end){
    this.folder = folder;
    try {
      this.msgCount = folder.getMessageCount();
    } catch (MessagingException e) {
      this.msgCount = SIZE_ERR;
    }
    this.batchSize = (batchSize == null)? msgCount : batchSize;
    
    this.start = (start == null)? 1 : start;
    this.end = (end == null)? msgCount : end;
    
    this.batchFirst = this.start;
    this.batchLast = this.start-1;
    
    messages = new Message[0];
//    if (!getNextBatch() || msgCount == SIZE_ERR) throw new RuntimeException("TODO:");//TODO

  }
  
  public Message getMessage(){
    return null;
  }
  
  @Override
  public boolean hasNext() {//TODO:search
    return buffIndex < Math.min(messages.length, end)
        || getNextBatch();
  }
  
  

  private boolean getNextBatch() {
    if(batchLast >= end){
      return false;
    }
    else {
      batchFirst = batchLast + 1;
      batchLast = Math.min(batchFirst + batchSize - 1, end);
      if(batchLast <= batchFirst) return false;
      try {
        messages = folder.getMessages(batchFirst, batchLast);
        buffIndex = 0;
        return messages != null && messages.length > 0;
      } catch (MessagingException e) {
        return false;
      }
    }
  }


  @Override
  public Message next() {
    return messages[buffIndex++];
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  @Override
  public void remove() {
      throw new UnsupportedOperationException();
  }
  
  public String getFolderName(){
    return folder != null ? folder.getName() : null;
  }

  
}
