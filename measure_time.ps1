# Configuration
$minSites = 500
$maxSites = 2000
$sitesStep = 500
$numClusters = 20
$runsPerConfig = 3
$mpjCommand = "mpjrun.bat"
$jarFile = "target\K-means-clustering.jar"
$outputFile = "results.csv"
$logFile = "execution_log.txt"

# Start logging
Start-Transcript -Path $logFile -Append
"Starting script at $(Get-Date)" | Out-File $logFile -Append
"Accumulation Sites, Average Runtime (ms)" | Out-File $outputFile -Append

# Loop through different accumulation site configurations
for ($sites = $minSites; $sites -le $maxSites; $sites += $sitesStep) {
    "Processing $sites accumulation sites" | Out-File $logFile -Append
    $totalTime = 0

    for ($run = 1; $run -le $runsPerConfig; $run++) {
        "Running $run for $sites accumulation sites..." | Out-File $logFile -Append

        # Capture start time
        $startTime = Get-Date

        # Run the Java program and wait for it to complete
        & $mpjCommand -np 4 -jar $jarFile --sites $sites --parallel --clusters $numClusters | Out-File $logFile -Append

        # Capture end time
        $endTime = Get-Date

        # Calculate runtime
        $runtime = ($endTime - $startTime).TotalMilliseconds
        "Run $run took $runtime milliseconds" | Out-File $logFile -Append
        $totalTime += $runtime
    }

    # Calculate average runtime
    $avgRuntime = $totalTime / $runsPerConfig
    "Average runtime for $sites accumulation sites: $avgRuntime milliseconds" | Out-File $logFile -Append
    "$sites, $avgRuntime" | Out-File $outputFile -Append
}

"Script completed at $(Get-Date)" | Out-File $logFile -Append
Stop-Transcript
