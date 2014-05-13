#!/usr/bin/perl

use strict;
use warnings;
use GD;

my $filename="usertest.csv";

open(FILE, $filename);
my %datapoints;

my $maxNumberOfRatings = 0;
while (<FILE>){
	my @parts = split(";");
	my $user = $parts[1];
	my $type = $parts[2];
	if (! exists $datapoints{$user}){
		$datapoints{$user} = [];
	}
	push @{$datapoints{$user}}, {"timestamp",$parts[0],"type",$type};
	if (scalar @{$datapoints{$user}} > $maxNumberOfRatings){
		$maxNumberOfRatings = scalar @{$datapoints{$user}};
	}
}
close FILE;

my $numberOfUsers = scalar keys %datapoints;

my $d = 50;
my $image = new GD::Image($numberOfUsers*$d,$maxNumberOfRatings*$d);

my $background = $image -> colorAllocate(255,255,255);
my $trendingColor = $image -> colorAllocate(192,24,24);
my $personalColor = $image -> colorAllocate(48,96,192);
my $sessionColor = $image -> colorAllocate(24,192,24);
my $black = $image -> colorAllocate(0,0,0);


my $x = 0;
my $y = 0;
foreach my $user (keys %datapoints){
	$y = 0;
	if ( scalar $datapoints{$user} > 0){
		my $lastClick = $datapoints{$user} -> [0] -> {"timestamp"};
		foreach my $click (@{$datapoints{$user}}){
			if ($click -> {"timestamp"} - $lastClick > 1000*60*30){
				$image -> filledRectangle($x,$y, $x+$d, $y+$d/2, $sessionColor);
				$y += $d/2;
			}
			if ($click -> {"type"} eq "trending"){
				
				$image -> filledRectangle($x,$y, $x+$d, $y+$d, $trendingColor);
			}
			else{
				$image -> filledRectangle($x,$y, $x+$d, $y+$d, $personalColor);
			}
		
			$image -> rectangle($x,$y, $x+$d, $y+$d, $black);
			$y += $d;
			$lastClick = $click -> {"timestamp"};
		}
	}
	$x += $d;
}

binmode STDOUT;
print $image->png;




