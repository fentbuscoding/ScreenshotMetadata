# PowerShell script to check PNG metadata
param([string]$ImagePath)

if (-not $ImagePath) {
    Write-Host "Usage: .\check_metadata.ps1 'path\to\screenshot.png'"
    exit
}

if (-not (Test-Path $ImagePath)) {
    Write-Host "File not found: $ImagePath"
    exit
}

Add-Type -AssemblyName System.Drawing

try {
    $image = [System.Drawing.Image]::FromFile($ImagePath)
    
    Write-Host "Image Properties for: $ImagePath"
    Write-Host "===================="
    
    # Check for standard properties that File Explorer can show
    $foundStandardProps = $false
    foreach ($propItem in $image.PropertyItems) {
        $key = $propItem.Id
        $value = [System.Text.Encoding]::UTF8.GetString($propItem.Value).TrimEnd([char]0)
        if ($value -ne "") {
            Write-Host "Property ID $key : $value"
            $foundStandardProps = $true
        }
    }
    
    if (-not $foundStandardProps) {
        Write-Host "No standard image properties found."
    }
    
    $image.Dispose()
    
    # Also check for PNG text chunks
    $bytes = [System.IO.File]::ReadAllBytes($ImagePath)
    
    Write-Host "`nSearching for PNG text chunks..."
    
    # Look for tEXt chunks and extract key-value pairs
    for ($i = 0; $i -lt $bytes.Length - 4; $i++) {
        # Look for "tEXt" chunk signature
        if ($bytes[$i] -eq 116 -and $bytes[$i+1] -eq 69 -and $bytes[$i+2] -eq 88 -and $bytes[$i+3] -eq 116) {
            # Found tEXt chunk, now find the actual text data
            # Skip back to find chunk length (4 bytes before tEXt)
            if ($i -ge 4) {
                $lengthBytes = $bytes[($i-4)..($i-1)]
                [Array]::Reverse($lengthBytes)  # Convert from big-endian
                $length = [BitConverter]::ToInt32($lengthBytes, 0)
                
                if ($length -gt 0 -and $length -lt 1000) {  # Reasonable length check
                    $textStart = $i + 4
                    $textEnd = $textStart + $length - 1
                    
                    if ($textEnd -lt $bytes.Length) {
                        $textBytes = $bytes[$textStart..$textEnd]
                        $textData = [System.Text.Encoding]::UTF8.GetString($textBytes).TrimEnd([char]0)
                        
                        # Split key and value (separated by null byte)
                        $nullIndex = $textData.IndexOf([char]0)
                        if ($nullIndex -gt 0) {
                            $key = $textData.Substring(0, $nullIndex)
                            $value = $textData.Substring($nullIndex + 1)
                            Write-Host "$key : $value"
                        }
                    }
                }
            }
        }
    }
    
} catch {
    Write-Host "Error reading image: $($_.Exception.Message)"
}
