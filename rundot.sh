
verbose=1
neato=0

log(){
    if [[ $verbose -gt 0 ]] ; then
        echo $*
    fi
}

processFile(){
    f=$1
    
    if [[ ! -f $f ]]; then
        echo "No such file or directory: $f"
       	exit 1
    fi 
    
    xf=${f%%.*}
    dot -Tpng -o"${xf}_dot.png" "${f}" 
    if [[ $neto -gt 0 ]]; then
        dot -Tpng -o"${xf}_neato.png" -Kneato "${f}" 
    fi
}

log "Processing DOT visualizations for $*"

for g in $*; do
    if [[ $verbose ]]; then
        log $g
    fi
    processFile $g
done

