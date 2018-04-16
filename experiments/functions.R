loadJavaCSV <- function(folder, input){
  size <- length(input)
  result <- c()
  
  for (i in 1:size) {
    cur <- paste0(folder,dir(path = folder, pattern = paste0(input[i], "")))
    result[[i]] <- lapply(seq_along(cur),function(x){
      assign(paste0(input[i], x), read.table(cur[x], header = TRUE, sep = ","))
    })
    
    minNumRows <- min(sapply(result[[i]], nrow))
    numObs <- length(result[[i]])
    tmp <- lapply(result[[i]], function(x){
      x <- x[1:minNumRows,]
    })
    tmp <- Reduce('+', tmp)
    tmp <- sapply(tmp, function(x){
      x <- x / numObs
    })
    result[[i]] <- data.frame(tmp)
  }
  
  names(result) <- input

  return(result)
}

plotResults <- function(x){
  
  size <- length(x)
  maxEval <- 0
  colours <- rainbow(size)
  
  for (i in 1:size) {
    tmpMax <- max(x[[i]]$evaluated)
    maxEval <- max(maxEval, tmpMax)
  }
  
  plot(x[[1]]$evaluated, x[[1]]$avgValue, xlim = c(0,maxEval), ylim = c(0.3,0.82), col=colours[1], xlab = c("evaluated levels"), ylab = c("average fitness"))
  abline(h=max(x[[1]]$avgValue), col=colours[1])
  
  for (i in 2:size) {
    points(x[[i]]$evaluated, x[[i]]$avgValue, col=colours[i])
    abline(h=max(x[[i]]$avgValue), col=colours[i])
  }
  
  legend("bottomright", legend=names(x),fill = colours)
}