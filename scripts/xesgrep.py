from pm4py.objects.log.importer.xes import importer
from pm4py.algo.filtering.log.attributes import attributes_filter
from pm4py.objects.log.exporter.xes import exporter as xes_exporter

import argparse
import sys

CONCEPT_NAME = "concept:name"

def filterfile(sourceFile,outputFile,patternText,inclusive):
    log = importer.apply(sourceFile)
    activities = attributes_filter.get_attribute_values(log, CONCEPT_NAME)
    filteredLog = attributes_filter.apply(log, [patternText],
            parameters={attributes_filter.Parameters.ATTRIBUTE_KEY: CONCEPT_NAME,
                        attributes_filter.Parameters.POSITIVE: inclusive})
    xes_exporter.apply(log, outputFile)    

def main():
    parser = argparse.ArgumentParser(
        description='Filter XES logs with grep-like arguments')
    parser.add_argument('filename', help='file to search')
    parser.add_argument('-e', '--pattern', help='activity name expression (not regex)')
    parser.add_argument('-v', '--invert-match',
                        help='select non-matching lines')
    parser.add_argument('--output', help='output file')
    args = parser.parse_args()
    if hasattr(args,'invert-match'):
        filterfile(args.filename,args.output,args.invert-match,False)
    else:
        filterfile(args.filename,args.output,args.pattern,True)


if __name__ == '__main__':
    main()
