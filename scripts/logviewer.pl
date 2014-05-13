#!/usr/bin/perl

use strict;
use warnings;
use GD;

my $filename="usertest.csv";

open(FILE, $filename);
my %datapoints;

my $numberOfClicks = 0;
my $maxduration = 20;

my $timePersonal = 0;
my $nPersonal = 0;
my $timeTrending = 0;
my $nTrending = 0;

my $maxNumberOfRatings = 0;
while (<FILE>){
	my @parts = split(";");
	my $user = $parts[1];
	my $type = $parts[2];
	if (! exists $datapoints{$user}){
		$datapoints{$user} = [];
	}
	my $prev = scalar @{$datapoints{$user}};
	my $diff = -1;
	if ($prev > 0){
		$diff = $parts[0] - $datapoints{$user} -> [$prev -1] -> {"timestamp"};
		$diff /= 1000;
		$diff /= 60;
		$diff = int($diff);
		if ($diff > $maxduration){
			$diff = -1;
		}
		if ($diff == 0){
			$diff = 1;
		}
	}
	push @{$datapoints{$user}}, {"timestamp",$parts[0],"type",$type,"diff",$diff};
	if (scalar @{$datapoints{$user}} > $maxNumberOfRatings){
		$maxNumberOfRatings = scalar @{$datapoints{$user}};
	}
}
close FILE;


my $numberOfUsers = scalar keys %datapoints;

my $maxY = 0;
foreach my $user (keys %datapoints){
	my $y = 0;
	
	for (my $i = 0; $i < scalar @{$datapoints{$user}}; $i++){
		$y += $datapoints{$user} -> [$i] -> {"diff"};
		if ($y > $maxY){
			$maxY = $y;
		}
	}	
}
my $d = 50;
my $image = new GD::Image($numberOfUsers*$d,$maxY*$d);

my $background = $image -> colorAllocate(255,255,255);
my $trendingColor = $image -> colorAllocate(192,24,24);
my $personalColor = $image -> colorAllocate(48,96,192);
my $sessionColor = $image -> colorAllocate(24,192,24);
my $black = $image -> colorAllocate(0,0,0);


my $x = 0;
my $y = 0;


foreach my $user (keys %datapoints){
	$y = 0;
	
	for (my $i = 0; $i < scalar @{$datapoints{$user}}; $i++){
		
		if( $datapoints{$user} -> [$i] -> {"diff"} > 0){		
			if ($datapoints{$user} -> [$i] -> {"diff"} > $maxduration){
				$image -> filledRectangle($x,$y, $x+$d, $y+$d/2, $sessionColor);
				$y += $d/2;
			}
			else{
				$numberOfClicks++;
				my $diff = $datapoints{$user} -> [$i] -> {"diff"};
				
				if ($datapoints{$user} -> [$i] -> {"type"} eq "trending"){
					$image -> filledRectangle($x,$y, $x+$d, $y+$diff*$d, $trendingColor);
					$image -> rectangle($x,$y, $x+$d, $y+$d*$diff, $black);
					$nTrending++;
					$timeTrending += $diff * 60;
				}
				else{
					$image -> filledRectangle($x,$y, $x+$d, $y+$diff*$d, $personalColor);
					$image -> rectangle($x,$y, $x+$d, $y+$d*$diff, $black);
					$nPersonal++;
					$timePersonal += $diff * 60;
				}
				$y += $diff * $d;

			}
		}		

	}
		$x += $d;
}


open(PICTURE, ">output.png") or die("Cannot open file for writing");

# Make sure we are writing to a binary stream
binmode PICTURE;

# Convert the image to PNG and print it to the file PICTURE
print PICTURE $image->png;
close PICTURE;


print "# users: \t", $numberOfUsers,"\n";
print "# clicks: \t", $numberOfClicks,"\n";
print "# trending clicks: \t", $nTrending,"\n";
print "# avg time trending: \t", $timeTrending / $nTrending,"\n";
print "# personal clicks: \t",$nPersonal,"\n";
print "# avg time personal: \t",$timePersonal / $nPersonal,"\n";

