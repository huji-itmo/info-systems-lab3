"use client";

import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Plus, Search, Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { components } from "@/types/api.types";
import { useCoordinates } from "@/hooks/use-coordinates-hooks";
import { CreateCoordinateDialogContent } from "./create-coordinate-dialog";

type CoordinatesWithId = components["schemas"]["CoordinatesWithId"];

interface CoordinateSelectorProps {
  onSelect: (coordinate: CoordinatesWithId) => void;
  selectedCoordinateId?: number;
  disabled?: boolean;
}

export function CoordinateSelector({ onSelect, selectedCoordinateId, disabled }: CoordinateSelectorProps) {
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const pageSize = 5
  const { data, isLoading, error } = useCoordinates(page, pageSize, {
    enabled: open, // Only fetch when dialog is open
  });

  const handleSelect = (coordinate: CoordinatesWithId) => {
    onSelect(coordinate);
    setOpen(false);
  };

  const handleCreateSuccess = (newCoordinate: CoordinatesWithId) => {
    setCreateDialogOpen(false);
    handleSelect(newCoordinate);
  };

  // Filter coordinates by search query (search by ID or coordinates)
  const filteredCoordinates = data?.content?.filter(coordinate => {
    const searchTerm = search.toLowerCase();
    return (
      coordinate.id?.toString().includes(searchTerm) ||
      coordinate.x.toString().includes(searchTerm) ||
      coordinate.y.toString().includes(searchTerm)
    );
  }) || [];

  return (
    <>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button
            variant="outline"
            className="justify-start w-full"
            disabled={disabled}
          >
            {selectedCoordinateId ? (
              <span>Coordinates ID: {selectedCoordinateId}</span>
            ) : (
              <span className="text-muted-foreground">Select Coordinates</span>
            )}
          </Button>
        </DialogTrigger>
        <DialogContent className="flex flex-col max-w-2xl max-h-[90vh] overflow-hidden">
          <DialogHeader>
            <DialogTitle>Select Coordinates</DialogTitle>
          </DialogHeader>

          <div className="flex items-center gap-2 py-4">
            <div className="relative flex-1">
              <Search className="top-2.5 left-2.5 absolute w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search coordinates (ID, X, Y)..."
                className="pl-8"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <Button onClick={(e) => {
              e.stopPropagation();
              setCreateDialogOpen(true);
            }}>
              <Plus className="mr-2 w-4 h-4" /> Create New
            </Button>
          </div>

          {isLoading ? (
            <div className="flex flex-1 justify-center items-center">
              <Loader2 className="w-8 h-8 text-muted-foreground animate-spin" />
            </div>
          ) : error ? (
            <div className="flex flex-col flex-1 justify-center items-center text-destructive">
              <p>Failed to load coordinates</p>
              <Button
                variant="outline"
                size="sm"
                className="mt-2"
                onClick={(e) => {
                  e.stopPropagation();
                  window.location.reload();
                }}
              >
                Retry
              </Button>
            </div>
          ) : (
            <>
              <div className="flex-1 overflow-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>X Coordinate</TableHead>
                      <TableHead>Y Coordinate</TableHead>
                      <TableHead className="w-[100px]">Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredCoordinates.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4} className="py-8 text-muted-foreground text-center">
                          {search ? "No matching coordinates found" : "No coordinates found"}
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredCoordinates.map((coordinate) => (
                        <TableRow
                          key={coordinate.id}
                          className={`cursor-pointer hover:bg-muted ${selectedCoordinateId === coordinate.id ? "bg-accent" : ""
                            }`}
                          onClick={() => handleSelect(coordinate)}
                        >
                          <TableCell className="font-medium">{coordinate.id}</TableCell>
                          <TableCell>{coordinate.x}</TableCell>
                          <TableCell>{coordinate.y.toFixed(2)}</TableCell>
                          <TableCell>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleSelect(coordinate);
                              }}
                            >
                              Select
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>

              {data && data.totalPages > 1 && (
                <div className="flex justify-center mt-4">
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious
                          onClick={() => setPage(prev => Math.max(0, prev - 1))}
                          className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                        />
                      </PaginationItem>

                      {/* Generate page items with max 5 visible */}
                      {(() => {
                        const totalPages = data.totalPages;
                        let items: (number | string)[] = [];

                        if (totalPages <= 5) {
                          // Show all pages if 5 or fewer
                          items = Array.from({ length: totalPages }, (_, i) => i + 1);
                        } else {
                          const current = page + 1; // Convert to 1-based index

                          items = [1];

                          // Add ellipsis and pages before current
                          if (current > 3) {
                            items.push('...');
                          }

                          // Add pages around current (max 3 pages)
                          const startPage = Math.max(2, current - 1);
                          const endPage = Math.min(totalPages - 1, current + 1);

                          for (let i = startPage; i <= endPage; i++) {
                            items.push(i);
                          }

                          // Add ellipsis and last page
                          if (current < totalPages - 2) {
                            items.push('...');
                          }

                          items.push(totalPages);
                        }

                        return items.map((item, index) => {
                          if (item === '...') {
                            return (
                              <PaginationItem key={`ellipsis-${index}`}>
                                <PaginationEllipsis />
                              </PaginationItem>
                            );
                          }

                          const pageNum = item as number;
                          return (
                            <PaginationItem key={pageNum}>
                              <PaginationLink
                                onClick={() => setPage(pageNum - 1)}
                                isActive={page === pageNum - 1}
                              >
                                {pageNum}
                              </PaginationLink>
                            </PaginationItem>
                          );
                        });
                      })()}

                      <PaginationItem>
                        <PaginationNext
                          onClick={() => setPage(prev => Math.min(data.totalPages - 1, prev + 1))}
                          className={page === data.totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              )}

              {/* Total count footer */}
              <div className="text-muted-foreground text-sm text-center">
                {data ? (
                  <>
                    Showing{" "}
                    <strong>
                      {page * pageSize + 1}–
                      {Math.min((page + 1) * pageSize, data.totalElements)}
                    </strong>{" "}
                    of <strong>{data.totalElements}</strong> Space Marines
                  </>
                ) : isLoading ? (
                  "Loading..."
                ) : null}
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>

      <CreateCoordinateDialogContent
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onSuccess={handleCreateSuccess}
      />
    </>
  );
}
