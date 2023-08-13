library(dplyr)
library(ggsci)

spmcolours <- function (){
	# three algos x six estimators + rsd
	fullpal <- pal_npg("nrc", alpha = 0.7)(4)
	algocol <- fullpal[1:3]
	rsdcol <- fullpal[4]
	spmcol <- append( rep( algocol, 6), rsdcol) 
}

export_graph <- function (workingPath, picName, bpo, 
		ctMeasure, ctLog, ctShortMeasure, colName, ylim)
{
	
	bpo <- rundata %>% filter (Log == ctLog)    # weird: overwrites param from global
	tb <- as.numeric(bpo[[colName]])
	bnames <- bpo$Short.Id
	addLabel <- FALSE
	mainLab = ""
	if (addLabel){
		mainLab = paste(ctMeasure, ctLog)
		picName <- paste(picName,"lab", sep="_");
	}
	# jpeg( paste(workingPath, picName,".jpg", sep="") )
	png( paste(workingPath, picName,".png", sep="") )
	par(mar = c(8,4,2,0))
	barplot(tb, main= mainLab, 
		names.arg = bnames, 
		ylab=ctShortMeasure,
		horiz=FALSE, cex.names=0.8, las=2,
		ylim = ylim,
		col=spmcolours() )
	dev.off()
}

em_graph <- function(workingPath, picName, rundata, ctLog){
	export_graph( workingPath, picName = picName, 
		  rundata, 
		  ctMeasure = "Earth Movers", 
		  ctLog = ctLog,
		  ctShortMeasure = "EM",
		  colName = "EARTH_MOVERS_LIGHT_COVERAGE",
		  ylim=c(0,1))
}

entp_graph <- function(workingPath, picName, rundata, ctLog){
	export_graph( workingPath, picName = picName, 
		  rundata, 
		  ctMeasure = "Entropy Precision", 
		  ctLog = ctLog,
		  ctShortMeasure = "H_P",
		  colName = "ENTROPY_PRECISION",
		  ylim=c(0,1))
}

entr_graph <- function(workingPath, picName, rundata, ctLog){
	export_graph( workingPath, picName = picName, 
		  rundata, 
		  ctMeasure = "Entropy Recall", 
		  ctLog = ctLog,
		  ctShortMeasure = "H_F",
		  colName = "ENTROPY_RECALL",
		  ylim=c(0,1))
}

entct_graph <- function(workingPath, picName, rundata, ctLog){
	export_graph( workingPath, picName = picName, 
		  rundata, 
		  ctMeasure = "Entity Count", 
		  ctLog = ctLog,
		  ctShortMeasure = "|P| + |T|",
		  colName = "MODEL_ENTITY_COUNT",
		  ylim=c(0,70))
}

edgect_graph <- function(workingPath, picName, rundata, ctLog){
	export_graph( workingPath, picName = picName, 
		  rundata, 
		  ctMeasure = "Entity Count", 
		  ctLog = ctLog,
		  ctShortMeasure = "|F|",
		  colName = "MODEL_EDGE_COUNT",
		  ylim=c(0,90))
}


workingPath = "c:/Users/burkeat/bpm/bpm-discover/var/"

#rundata = read.csv( paste(workingPath,"paper.psv", sep=""), 
#			sep ="|", strip.white=TRUE)

rundata = read.csv( paste(workingPath,"results202308.psv", sep=""), 
		  sep ="|", strip.white=TRUE)


clncreators <- recode(rundata$Short.Id,
				   "align-fodina"   = "walign-fodina",
				   "align-inductive" = "walign-inductive",
				   "align-split"    = "walign-split",
				   "aplh-fodina" 	  = "wlhpair-fodina",
				   "aplh-inductive" = "wlhpair-inductive",
				   "aplh-split"     = "wlhpair-split",
				   "aprh-fodina"    = "wrhpair-fodina",
				   "aprh-inductive" = "wrhpair-inductive",
				   "aprh-split"     = "wrhpair-split",
				   "bce-fodina" 	  = "wfork-fodina",
				   "bce-inductive"  = "wfork-inductive",
				   "bce-split" 	  = "wfork-split",
				   "fe-fodina" 	  = "wfreq-fodina",
				   "fe-inductive"   = "wfreq-inductive",
				   "fe-split" 	  = "wfreq-split",
				   "msaprh-fodina"  = "wpairscale-fodina",
				   "msaprh-inductive" = "wpairscale-inductive",
				   "msaprh-split"   = "wpairscale-split",
				   "rssmt" = "gdt_spn")


rundata$Short.Id <- clncreators

logs <- unique(rundata$Log)

empicName <- paste("em", gsub(" ","_", tolower(logs)), sep="") 
entppicName <- paste("entp", gsub(" ","_", tolower(logs)), sep="") 
entrpicName <- paste("entr", gsub(" ","_", tolower(logs)), sep="") 
entcpicName <- paste("entc", gsub(" ","_", tolower(logs)), sep="") 
edgepicName <- paste("edge", gsub(" ","_", tolower(logs)), sep="") 
durpicName <- paste("dur", gsub(" ","_", tolower(logs)), sep="") 

# reset default margins

count <- 1
for (log in logs){
	em_graph( workingPath, picName = empicName[count], 
			  rundata, ctLog = log )
	entp_graph( workingPath, picName = entppicName[count], 
		  rundata, ctLog = log )
	entr_graph( workingPath, picName = entrpicName[count], 
		  rundata, ctLog = log )
	count= count +1
}



