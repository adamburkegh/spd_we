
library(dplyr)
library(pals)

plot_duration_line <- function (peralgo,xvar, artifactId, colour)
{
	tctlog <- peralgo[order( peralgo[[xvar]]), ]
	lines( tctlog[[xvar]], 
		tctlog$Miner.Duration + tctlog$Estimator.Duration, , 
		type="o", col=colour)
}

plot_duration <- function(workingPath,rundata,creators,xvar,xlab,colours,
					exportPic = FALSE, ylab, yaxt='s')
{
	# Layout plot
	peralgo <- rundata %>% filter ( Short.Id == "rssmt" & Miner.Duration > 0)
	tctlog <- peralgo[order( peralgo[[xvar]]), ]
	ymax <- max(tctlog$Miner.Duration)
	ylim <- c(1, ymax)
	plot( tctlog[[xvar]], 
			tctlog$Miner.Duration, 
			type="p", 
			xlab=xlab, 
			ylab = ylab,
			ylim=ylim, 
			log="y",
			yaxt=yaxt)

	nac <- length(creators)
	for (i in 1:nac){
		if ( creators[i] != "rssmt" ){
			peralgo <- rundata %>% 
				filter ( Short.Id == creators[i] & Estimator.Duration > 0) 
		}else{
			peralgo <- rundata %>% 
				filter ( Short.Id == creators[i] & Miner.Duration > 0) 
		}
		plot_duration_line(peralgo,xvar, 
			creators[i], colours[i])
	}

}

dev.off()
dev.new()

workingPath = "c:/Users/burkeat/bpm/bpm-discover/var/"

rundata = read.csv( paste(workingPath,"paper.psv", sep=""),
            sep ="|", strip.white=TRUE)

exportPic <- FALSE

# Probably brutal style but I'll live
rundata$MODEL_ENTITY_COUNT <- as.numeric(rundata$MODEL_ENTITY_COUNT)
rundata$MODEL_EDGE_COUNT <- as.numeric(rundata$MODEL_EDGE_COUNT)
rundata$LOG_EVENT_COUNT <- as.numeric(rundata$LOG_EVENT_COUNT)
rundata$LOG_TRACE_COUNT <- as.numeric(rundata$LOG_TRACE_COUNT)
rundata$Miner.Duration <- as.numeric(rundata$Miner.Duration)
rundata$Estimator.Duration <- as.numeric(rundata$Estimator.Duration)

if (exportPic){
	png( paste(workingPath, "rt.png", sep=""), res=300 )
}


logs <- unique(rundata$Log)
creators <- unique(rundata$Short.Id)

colours <- alphabet()

par(fig=c(0,0.58,0,1), new=TRUE)
plot_duration(workingPath,rundata,creators,"LOG_EVENT_COUNT","Number of events",
			colours,exportPic=exportPic, ylab=expression('log'[10]*'t (ms)' ))
par(fig=c(0.42,1,0,1), new=TRUE)
plot_duration(workingPath,rundata,creators,"LOG_TRACE_COUNT","Number of traces",
			colours,exportPic=exportPic, ylab="", yaxt='n')

clncreators <- recode(creators,  "bce-fodina" = "fork-fodina",
					   "bce-inductive" = "fork-inductive",
					   "bce-split" = "fork-split",
					   "rssmt" = "rsd")
legend(x=max(tctlog[[xvar]])*0.5,y=1000,legend=clncreators,
	col=colours,cex=0.7,lty=rep(c(1),nac),
	title="Artifact Creators")

if (exportPic){
	dev.off()
}