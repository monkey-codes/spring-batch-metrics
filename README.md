#Limitations

* Jobs must have a processor. The state machine relies on
some of the processor events to create synthetic balanced state events. You can use
PassThroughItemProcessor provided by batch framework when no processing is required on the job.

* Once a Process or Write error happens in a chunk, the subsequent re process chunk
will not emit processing or write events. 
  * Emitting processing events in this state may yield process counts higher that the number of
  read items because if a chunk contains more than 1 process error the same item may be processed
  several times while the framework whittles down the chunk to only the items that pass processing.
  * Emitting write events will also skew numbers, since write events in this case will happen on smaller chunks. In 
   case of write events itself, write will be called with a chunk of 1 to figure out where in the original 
   chunk the write error happend.
 