package org.pentaho.di.trans.steps.mailinput;

import java.util.Iterator;

import javax.mail.Message;
import javax.mail.search.SearchTerm;

public class SearchEnabledFolderIterator implements Iterator<Message> {

  private Iterator<Message> iterator;
  private SearchTerm searchTerm;
  
  private Message next;
  
  public SearchEnabledFolderIterator(Iterator<Message> messageIterator, SearchTerm search){
    this.iterator = messageIterator;
    this.searchTerm = search;
    fetchNext();
  }
  
  @Override
  public boolean hasNext() {
    return next != null && searchTerm.match(next);
  }

  @Override
  public Message next() {
    Message toReturn = next;
    
    fetchNext();
    
    return toReturn;
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  @Override
  public void remove() {
      throw new UnsupportedOperationException();
  }
  
  private void fetchNext(){

    while(iterator.hasNext()){
      next = iterator.next();
      if(searchTerm.match(next)){
        return;
      }
      else if(!iterator.hasNext()){
        break;
      }
    }
    next=null;
    
  }

}
