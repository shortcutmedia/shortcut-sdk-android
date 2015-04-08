/*
 * Copyright 2015 Shortcut Media AG.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.scm.reader.livescanner.sdk;/*


/**
 * Interface for listening to different recognition events.
 * <ul>
 *   <li>
 *     <strong>onImageRecognized</strong>: fired when an image has been recognized. Info about the
 *     recognition you can find in the {@param event}.
 *   </li>
 *   <li>
 *     <strong>onImageNotRecognized</strong>: fired when an image hasn't been recognized.     recognition you can find in the {@param event}.
 *   </li>
 *   <li>
 *     <strong>onError</strong>: fired when there has been an exception while trying to perform.
 *     the recognition. {@param exception} is the expcetion that was raised.
 *   </li>
 *   <li>
 *     <strong>onInfo</strong>: Some text has been sent. Used for debugging.
 *   </li>
 * </ul>
 */
public interface KEventListener {
  public void onImageRecognized(KEvent event);
  public void onImageNotRecognized(KEvent event);
  public void onError(Exception exception);
  public void onInfo(String message);
  public void onContinueKooabaRecognition(String message);
  public void onPauseKooabaRecognition(String message);
 
}
