#!/bin/bash

# Configuration
min_sites=500
max_sites=2000
sites_step=500
num_clusters=20
runs_per_config=3
jar_file="target/K-means-clustering.jar"
output_file="results.csv"

# Debug: Echo the paths to verify
echo "Jar file path: $jar_file"
echo "Output file path: $output_file"

# Write CSV header
echo "Accumulation Sites,Average Runtime (ms)" > $output_file

# Loop through different accumulation site configurations
for ((sites=$min_sites; sites<=$max_sites; sites+=$sites_step))
do
    echo "Processing $sites accumulation sites"
    total_time=0

    for ((run=1; run<=$runs_per_config; run++))
    do
        echo "Running $run for $sites accumulation sites..."

        # Capture start time
        start_time=$(date +%s%3N)

        # Run the Java program and measure time
        java -jar $jar_file --sites $sites --parallel --clusters $num_clusters

        # Capture end time
        end_time=$(date +%s%3N)

        # Calculate runtime in milliseconds
        runtime=$((end_time - start_time))
        echo "Run $run took $runtime milliseconds"
        total_time=$((total_time + runtime))
    done

    # Calculate average runtime
    avg_runtime=$((total_time / runs_per_config))
    echo "Average runtime for $sites accumulation sites: $avg_runtime milliseconds"

    # Append result to CSV file
    echo "$sites,$avg_runtime" >> $output_file
done

echo "All configurations complete. Results saved to $output_file."
