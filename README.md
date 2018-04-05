#Limitations

* Jobs must have a processor. The state machine relies on
some of the processor events to create synthetic balanced state events. You can use
PassThroughItemProcessor provided by batch framework when no processing is required on the job.
 